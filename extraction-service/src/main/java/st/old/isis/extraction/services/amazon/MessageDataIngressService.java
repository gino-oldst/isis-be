package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import st.old.isis.dto.aws.AwsS3BucketObject;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.enums.Source;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.context.DownloadContext;
import st.old.isis.extraction.entities.FileDownloadHistory;
import st.old.isis.extraction.properties.AwsS3Properties;
import st.old.isis.extraction.services.BloombergChatExtractionService;
import st.old.isis.extraction.services.BloombergEmailExtractionService;
import st.old.isis.extraction.services.FileDownloadHistoryService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.extraction.utils.DownloadUtils;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDataIngressService {

    private final AmazonS3ClientService amazonS3ClientService;

    private final BloombergChatExtractionService bloombergChatExtractionService;
    private final BloombergEmailExtractionService emailExtractionService;

    private final AwsS3Properties awsS3Properties;
    private final StorageService storageService;
    private final FileDownloadHistoryService historyService;

    private final Pattern CHAT_FILE_PATTERN = Pattern.compile("^f840690.ib19.*\\.xml$");
    private final Pattern EMAIL_FILE_PATTERN = Pattern.compile("^f840690.msg.*\\.xml$");

    protected String source() {
        return Source.BLOOMBERG.name();
    }

    public void ingressAll(@RequestBody DataImportRecordDto request) {

        List<S3ObjectSummary> listOfSummaries = amazonS3ClientService.retrieveRawMessageDataFromSource(request.getSource());

        listOfSummaries.stream().forEach(o -> {
            try {
                inspectItem(o, request.getImportDate(), request.getSource());
            } catch (Exception e) {
                e.printStackTrace();
                sendDownloadingError(e);
            }
        });

    }

    private void inspectItem (
            @NonNull final S3ObjectSummary os,
            @NonNull final LocalDate importDate,
            @NonNull final String source) throws IsisException, XMLStreamException, JAXBException {

        String objectKey = os.getKey();
        final var keys = objectKey.split(DownloadUtils.S3_DELIMITER);

        String fileName = keys[2];

        if (skipFile(fileName)) {
            log.info("File ignored: {}", objectKey);
            return;
        }

        log.info("Processing - {}", objectKey);

        final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), objectKey);

        boolean isSuccess= false;

//        if(isChat(fileName)){
//            // TODO Will set import date manual for now, replace parse with processXML once scheduled jobs is activated
////        bloombergChatExtractionService.processXml(s3Object.getObjectContent(), source);
//            bloombergChatExtractionService.parse(s3Object.getObjectContent(), importDate, source);
//            isSuccess = true;
//        }

        if(isEmail(fileName)){
            // TODO Will set import date manual for now, replace parse with processXML once scheduled jobs is activated
//            emailExtractionService.processXml(s3Object.getObjectContent(), source);
            emailExtractionService.parse(s3Object.getObjectContent(), importDate, source);
            isSuccess = true;
        }

        if(isSuccess){
            storageService.merge(DownloadContext.getBatchFilesMap());

            // TODO Indexing
            // Transfer to processed bucket
            amazonS3ClientService.moveFileToIngressBucket(AwsS3BucketObject.builder()
                    .sourceBucket(os.getBucketName())
                    .sourceKey(objectKey)
                    .targetBucket(awsS3Properties.getIngressBucket())
                    .targetKey(objectKey).build());
        }

    }

    public void ingressFile() throws IsisException {
        ObjectListing objectListing = amazonS3ClientService.listObjects(awsS3Properties.getRawBucket());
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(os.getKey());
            final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), os.getKey());
            log.info(s3Object.toString());
        }
        storageService.merge(DownloadContext.getBatchFilesMap());
    }

    protected void saveToHistory(@NonNull final String sha1, @NonNull final String filePath) {

        final var history = FileDownloadHistory.builder().id(sha1).filePath(filePath).build();

        historyService.saveHistory(history);
    }

    protected boolean skipFile(@NonNull final String filename) {
        return filenamePatterns().stream().noneMatch(pattern -> pattern.matcher(filename).find());
    }

    private List<Pattern> filenamePatterns() {
        return List.of(CHAT_FILE_PATTERN, EMAIL_FILE_PATTERN);
    }

    private void sendDownloadingError(Throwable err) {
        log.error(String.format("An error occurred when downloading %s raw file", source()), err);

        final var request =
                NotificationRequestDto.builder()
                        .customMessage(
                                MessageDto.builder()
                                        .subject("Download Failed")
                                        .body(
                                                String.format(
                                                        "Download %s raw date file failed: %s", source(), err.getMessage()))
                                        .build())
                        // .recipients() todo: recipients
                        .build();

        //    notificationService.sendNotification(List.of(request));
    }

    private boolean isChat(@NonNull final String filename) {
        return CHAT_FILE_PATTERN.matcher(filename).find();
    }

    private boolean isEmail(@NonNull final String filename) {
        return EMAIL_FILE_PATTERN.matcher(filename).find();
    }

}
