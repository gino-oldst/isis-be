package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.entities.FileDownloadHistory;
import st.old.isis.extraction.properties.AwsS3Properties;
import st.old.isis.extraction.services.FileDownloadHistoryService;
import st.old.isis.extraction.services.internal.AuditService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.extraction.utils.DownloadUtils;
import st.old.isis.utils.AuditUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public abstract class RawDownloadService {

    @Autowired private AmazonS3ClientService amazonS3ClientService;
    @Autowired private AwsS3Properties awsS3Properties;
    @Autowired private StorageService storageService;
    @Autowired protected AuditUtil auditUtil;
    @Autowired protected AuditService auditService;
    @Autowired private FileDownloadHistoryService historyService;
    @Autowired protected DownloadUtils downloadUtils;

    protected abstract String source();

    protected abstract void processFile(S3Object s3Object, LocalDate importDate, String source) throws IsisException;

    protected abstract boolean skipFile(@NonNull final String filename);

    protected abstract boolean isDirectoryValid (String key);

    protected abstract LocalDate parseToImportDate(String key);

    protected abstract String parseToS3FolderDate(LocalDate originalDate);

    @Async
    public void syncYesterdayDate() {
        syncDate(LocalDate.now().minusDays(1));
    }
    @Async
    public void syncAll(DataImportRecordDto request) {
        syncAll(request.getImportDate());
    }

    @Async
    public void syncDate(LocalDate importDate) {

        List<S3ObjectSummary> listOfSummaries = retrieveRawMessageDataFromSourceByDate(importDate);

        listOfSummaries.stream().forEach(o -> {
            try {
                inspectItem(o, importDate, source());
            } catch (Exception e) {
                e.printStackTrace();
                sendDownloadingError(e);
            }
        });

    }

    @Async
    public void syncAll(LocalDate importDate) {

        List<S3ObjectSummary> listOfSummaries = retrieveRawMessageDataFromSource();

        listOfSummaries.stream().forEach(o -> {
            try {
                inspectItem(o, importDate, source());
            } catch (Exception e) {
                e.printStackTrace();
                sendDownloadingError(e);
            }
        });

    }

    private void inspectItem (
            @NonNull final S3ObjectSummary os,
            final LocalDate importDate,
            @NonNull final String source) throws IsisException {

        String objectKey = os.getKey();

        String fileName = getS3FileName(objectKey);
        LocalDate processedImportDate = importDate;

        if(importDate == null){
            processedImportDate = parseToImportDate(objectKey);
        }

        if (skipFile(fileName)) {
            log.info("File ignored: {}", objectKey);
            return;
        }

        if(skipFileBasedOnFileIdAndKey(os.getETag(), objectKey)){
            log.info("File already ingested: eTag {} with filePath {}", os.getETag(), objectKey);
            return;
        }

        log.info("Processing - {} for Import Date - {}", objectKey, processedImportDate);

        final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), objectKey);


        try {

            saveToHistory(importDate, os.getETag(), objectKey, ProcessedStatus.PENDING_PROCESS);

            Instant start = Instant.now();

            processFile(s3Object, processedImportDate, source());

//          storageService.merge(DownloadContext.getBatchFilesMap());

            historyService.completeHistory(os.getETag());

            // @Deprecated - 20230106 - Started using OpenSearch so there's no need to do our own indexing
            // Transfer to processed bucket
//            amazonS3ClientService.moveFileToProcessedBucket(AwsS3BucketObject.builder()
//                    .sourceBucket(os.getBucketName())
//                    .sourceKey(objectKey)
//                    .targetBucket(awsS3Properties.getIngressBucket())
//                    .targetKey(objectKey).build());

            Instant end = Instant.now();

            log.info("Successfully completed processing S3 file {} in {}", objectKey, Duration.between(start, end).toString());

        } catch (IsisException ie) {
            log.error("Error processing raw file: {} - {}", source, ie.getMessage());
            historyService.failHistory(os.getETag());
            throw ie;
        }

    }

    protected List<S3ObjectSummary> retrieveRawMessageDataFromSource() {

        final var directory = downloadUtils.determineDirectoryFromSource(source());

        ListObjectsRequest listObjectsRequest= new ListObjectsRequest().withBucketName(awsS3Properties.getRawBucket()).withPrefix(directory);
        ObjectListing listObjectsResponse = amazonS3ClientService.listObjects(listObjectsRequest);

        List<S3ObjectSummary> listSummaries = new ArrayList<>();
        Set<String> processedDates= new HashSet<>();

        for(S3ObjectSummary os : listObjectsResponse.getObjectSummaries()) {
            String objectKey = os.getKey();
            final var keys = Arrays.asList(objectKey.split("/"));

            if(keys.size() > 1 && keys.get(1) != null && !processedDates.contains(keys.get(1)) && isDirectoryValid(keys.get(1))){
                String folderDate = keys.get(1);
                log.info("Date Bucket found! {}/{} for source {}", keys.get(0), folderDate, source());
                String delimitedKey = keys.get(0) + DownloadUtils.S3_DELIMITER + folderDate + DownloadUtils.S3_DELIMITER;
                ObjectListing subFolderListing = amazonS3ClientService.listFilesByBucketAndPrefix(awsS3Properties.getRawBucket(), delimitedKey);
                subFolderListing.getObjectSummaries().stream().filter(so -> !so.getKey().endsWith(DownloadUtils.S3_DELIMITER)).forEach(so -> {
                    listSummaries.add(so);
                });
                processedDates.add(folderDate);
            }
        }

        return listSummaries;
    }

    protected List<S3ObjectSummary> retrieveRawMessageDataFromSourceByDate(LocalDate targetDate) {

        final var directory = downloadUtils.determineDirectoryFromSource(source());

        ListObjectsRequest listObjectsRequest= new ListObjectsRequest().withBucketName(awsS3Properties.getRawBucket()).withPrefix(directory + DownloadUtils.S3_DELIMITER + parseToS3FolderDate(targetDate));
        ObjectListing listObjectsResponse = amazonS3ClientService.listObjects(listObjectsRequest);

        List<S3ObjectSummary> listSummaries = new ArrayList<>();
        Set<String> processedDates= new HashSet<>();

        for(S3ObjectSummary os : listObjectsResponse.getObjectSummaries()) {
            String objectKey = os.getKey();
            final var keys = Arrays.asList(objectKey.split("/"));

            if(keys.size() > 1 && keys.get(1) != null && !processedDates.contains(keys.get(1)) && isDirectoryValid(keys.get(1))){
                String folderDate = keys.get(1);
                log.info("Date Bucket found! {}/{} for source {}", keys.get(0), folderDate, source());
                String delimitedKey = keys.get(0) + DownloadUtils.S3_DELIMITER + folderDate + DownloadUtils.S3_DELIMITER;
                ObjectListing subFolderListing = amazonS3ClientService.listFilesByBucketAndPrefix(awsS3Properties.getRawBucket(), delimitedKey);
                subFolderListing.getObjectSummaries().stream().filter(so -> !so.getKey().endsWith(DownloadUtils.S3_DELIMITER)).forEach(so -> {
                    listSummaries.add(so);
                });
                processedDates.add(folderDate);
            }
        }

        return listSummaries;
    }

    protected String getS3FileName(String objectKey) {
        final var keys = objectKey.split(DownloadUtils.S3_DELIMITER);
        return keys[2];
    }

    protected static String getFileDate(String objectKey) {
        final var keys = objectKey.split(DownloadUtils.S3_DELIMITER);
        return keys[1];
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

    protected void saveToHistory(LocalDate importDate, @NonNull final String sha1, @NonNull final String filePath, @NonNull final ProcessedStatus status) throws IsisException {

        final var history = FileDownloadHistory.builder()
                .id(sha1)
                .filePath(filePath)
                .importDate(importDate)
                .status(status)
                .build();

        historyService.saveHistory(history);
    }
    private boolean skipFileBasedOnFileIdAndKey(@NonNull final String fileId, @NonNull final String key) {

        if (Strings.isEmpty(fileId)) {
            log.info("Skipping file id is empty for file name: [{}]", key);
            return true;
        }

        final var isFileIdExisting = historyService.isExistingByIdAndKeyAndStatus(fileId, key, ProcessedStatus.COMPLETED);

        if (isFileIdExisting) {
            log.info("Skipping file id is existing: [{}]", fileId);
            return true;
        }

        return false;
    }

}
