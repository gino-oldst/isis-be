package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.rds.model.SourceNotFoundException;
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
import st.old.isis.dto.util.ProcessRequestDto;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.enums.SourceType;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.context.DownloadContext;
import st.old.isis.extraction.entities.FileDownloadHistory;
import st.old.isis.extraction.properties.AwsS3Properties;
import st.old.isis.extraction.services.FileDownloadHistoryService;
import st.old.isis.extraction.services.internal.AuditService;
import st.old.isis.extraction.services.internal.DataImportRecordService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.extraction.utils.DownloadUtils;
import st.old.isis.utils.AuditUtil;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

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

    @Autowired protected DataImportRecordService dataImportRecordService;

    protected abstract String source();

    protected abstract void processFile(S3Object s3Object, LocalDate importDate, String source) throws IsisException;
    protected abstract void processInputStream(InputStream inputStream, String fileName, LocalDate importDate, String source) throws IsisException;

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
    public void syncByDateRange(ProcessRequestDto request) {

        final var summariesMap = retrieveRawMessageDataFromSourceByDateRange(request);

        summariesMap.keySet().stream().forEach(targetDate -> {
            final var listOfSummaries = summariesMap.get(targetDate);
            listOfSummaries.stream().forEach(o -> {
                try {
                    inspectItem(o, targetDate, source());
                } catch (Exception e) {
                    e.printStackTrace();
                    sendDownloadingError(e);
                }
            });
        });

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
            @NonNull final String source) throws IsisException, IOException {

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

        Instant start = Instant.now();

        log.info("Processing - {} for Import Date - {} - Started {}", objectKey, processedImportDate, start);

        try {

            String filePath;

            try(final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), objectKey)){
                filePath =  copyS3FileToLocalDirectory(s3Object, fileName);
            }

            saveToHistory(importDate, os.getETag(), objectKey, ProcessedStatus.PENDING_PROCESS);

            File localFile = new File(filePath);
            try (InputStream localFileStream = new FileInputStream(localFile)){
                processInputStream(localFileStream, fileName, processedImportDate, source());
            }

            log.info("Completing History - {} for Import Date - {}", os.getETag(), importDate);
            historyService.completeHistory(os.getETag());

            final var dataImport = DataImportRecordDto.builder()
                    .importDate(importDate)
                    .processedStatus(ProcessedStatus.PENDING_PROCESS)
                    .source(determineSpecificSource(fileName))
                    .build();

            dataImportRecordService.create(dataImport);

            // @Deprecated - 20230106 - Started using OpenSearch so there's no need to do our own indexing
            // Transfer to processed bucket
//            amazonS3ClientService.moveFileToProcessedBucket(AwsS3BucketObject.builder()
//                    .sourceBucket(os.getBucketName())
//                    .sourceKey(objectKey)
//                    .targetBucket(awsS3Properties.getIngressBucket())
//                    .targetKey(objectKey).build());

            log.info("Deleting s3Object {} from local folder", filePath);
            localFile.delete();

            Instant end = Instant.now();

            log.info("Successfully completed processing S3 file {} in {}", objectKey, Duration.between(start, end).toString());

        } catch (IsisException ie) {
            Instant end = Instant.now();
            log.error("Error processing raw file: {} - {}", source, ie.getMessage());
            log.info("Failed processing S3 file {} in {}", objectKey, Duration.between(start, end).toString());
            historyService.failHistory(os.getETag(), ie.getCause().getMessage());
            throw ie;
        } finally {
            log.info("STARTED - Merging Batch Files Map");
            storageService.merge(DownloadContext.getBatchFilesMap());
            log.info("COMPLETED - Merging Batch Files Map");
        }
    }

    private String copyS3FileToLocalDirectory(S3Object s3Object, String fileName) throws IOException {

        final var homeDir = System.getProperty("user.home");
        final var separator = System.getProperty("file.separator");

        final var tmpDownloadDir = new StringBuilder(homeDir).append(separator).append("tmpDownload");
        File directory = new File(String.valueOf(tmpDownloadDir));

        if(!directory.exists()){
            directory.mkdir();
        }

        final var finalPath = tmpDownloadDir.append(separator).append(fileName).toString();

        log.info("Copying filename {} to directory to {}", fileName, finalPath);

        ReadableByteChannel readableByteChannel = Channels.newChannel(s3Object.getObjectContent());

        try(FileOutputStream fileOutputStream = new FileOutputStream(finalPath, false)){
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }

        return finalPath;

    }

    private String determineSpecificSource(String filename) throws IsisException {

        final Pattern BLOOMBERG_CHAT_FILE_PATTERN = Pattern.compile("^f840690.ib19.*\\.xml$");
        final Pattern BLOOMBERG_EMAIL_FILE_PATTERN = Pattern.compile("^f840690.msg.*\\.xml$");
        final Pattern REUTERS_FILE_PATTERN = Pattern.compile("^messages.*\\.zip$");

        if(BLOOMBERG_CHAT_FILE_PATTERN.matcher(filename).find()){
            return SourceType.BLOOMBERG_CHAT.name();
        } else if(BLOOMBERG_EMAIL_FILE_PATTERN.matcher(filename).find()){
            return SourceType.BLOOMBERG_MAIL.name();
        } else if(REUTERS_FILE_PATTERN.matcher(filename).find()) {
            return SourceType.REUTERS.name();
        } else {
            throw IsisException.notFound("File with name" + filename + " did not match any sources", "SourceNotFoundException");
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

    protected Map<LocalDate, List<S3ObjectSummary>> retrieveRawMessageDataFromSourceByDateRange(ProcessRequestDto request) {

        final var directory = downloadUtils.determineDirectoryFromSource(source());

        Map<LocalDate, List<S3ObjectSummary>> summariesMap = new HashMap<>();
        Set<String> processedDates= new HashSet<>();

        final var inclusiveEndDate= request.getDateTo().plusDays(1);

        for(LocalDate targetDate = request.getDateFrom(); targetDate.isBefore(inclusiveEndDate); targetDate = targetDate.plusDays(1)){

            ListObjectsRequest listObjectsRequest= new ListObjectsRequest().withBucketName(awsS3Properties.getRawBucket()).withPrefix(directory + DownloadUtils.S3_DELIMITER + parseToS3FolderDate(targetDate));
            ObjectListing listObjectsResponse = amazonS3ClientService.listObjects(listObjectsRequest);

            List<S3ObjectSummary> listSummaries = new ArrayList<>();

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

            if(!listSummaries.isEmpty()){
                summariesMap.put(targetDate, listSummaries);
            }
        }

        return summariesMap;
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

        log.info("Saving History - {} for Import Date - {} - Status {}", sha1, importDate, status);

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
