package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.aws.AwsS3BucketObject;
import st.old.isis.extraction.properties.AwsS3Properties;
import st.old.isis.extraction.utils.DownloadUtils;

import java.util.Arrays;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class AmazonS3ClientService {

    private final AmazonS3 amazonS3Client;
    private final AwsS3Properties awsS3Properties;
    private final DownloadUtils downloadUtils;

    public void listAllRawBucketFiles() {
        ObjectListing objectListing = amazonS3Client.listObjects(awsS3Properties.getRawBucket());
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            if(os.getKey().endsWith("/")){
                log.info("Bucket found! - {}", os.getKey());
                ObjectListing subFolderListing = listObjects(awsS3Properties.getRawBucket(), os.getKey());

                subFolderListing.getObjectSummaries().stream().forEach(so -> {
                    log.info(so.getKey());
                });
            } else {
                log.info(">> {}", os.getKey());
            }
        }
    }

    public void listRawSpecificSource(String source) {

        final var directory = downloadUtils.determineDirectoryFromSource(source);

        ListObjectsV2Request listObjectsRequest= new ListObjectsV2Request().withBucketName(awsS3Properties.getRawBucket()).withPrefix(directory);
        ListObjectsV2Result listObjectsResponse = amazonS3Client.listObjectsV2(listObjectsRequest);

        for(S3ObjectSummary os : listObjectsResponse.getObjectSummaries()) {
            String objectKey = os.getKey();
            final var keys = Arrays.asList(objectKey.split("/"));
            if(objectKey.endsWith("/") && keys.size() > 1 && keys.get(1) != null && keys.get(1).matches("^[0-9]{4,6}$")){
                log.info("Date Bucket found! - {}", keys.get(1));
                ObjectListing subFolderListing = listRawSpecificFolder(awsS3Properties.getRawBucket(), keys.get(1));
                subFolderListing.getObjectSummaries().stream().forEach(so -> {
                    log.info(so.getKey());
                });
            }
        }
    }

    public ObjectListing listRawSpecificFolder(String bucket, String marker) {
        ListObjectsRequest listObjectsRequest= new ListObjectsRequest().withBucketName(bucket).withDelimiter("/").withMarker(marker);
        return amazonS3Client.listObjects(listObjectsRequest);
    }

    public void listRawSpecificFolder(String marker) {

        ListObjectsRequest listObjectsRequest= new ListObjectsRequest().withBucketName(awsS3Properties.getRawBucket()).withDelimiter("/").withMarker(marker);
        ObjectListing objectListing = amazonS3Client.listObjects(listObjectsRequest);

        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(">> {}", os.getKey());
        }
    }

    public ObjectListing listObjects(String bucket){
        return amazonS3Client.listObjects(bucket);
    }

    public ObjectListing listObjects(String bucket, String prefix){
        ListObjectsRequest listObjectsRequest= new ListObjectsRequest().withBucketName(bucket).withDelimiter("/").withPrefix(prefix);
        return amazonS3Client.listObjects(listObjectsRequest);
    }

    public S3Object downloadS3Object(String bucket, String objectKey){
        S3Object s3object = amazonS3Client.getObject(bucket, objectKey);
        log.info("Downloaded s3 object", s3object.toString());
        return s3object;
    }

    public void moveFileToIngressBucket(AwsS3BucketObject bucketObject){
        String targetKey = bucketObject.getSourceKey(); //TODO For future purposes, determine target key for indexing
        moveFileToTargetLocation(bucketObject.getSourceBucket(), bucketObject.getSourceKey(), awsS3Properties.getIngressBucket(), targetKey);
        log.info("Moved S3 object key {} from bucket {} to ingress bucket {} key {}", bucketObject.getSourceKey(), bucketObject.getSourceBucket(), awsS3Properties.getIngressBucket(), targetKey);
    }

    public void moveFileToTargetLocation(String sourceBucket, String sourceKey, String targetBucket, String targetKey){
        copyFileToTargetLocation(sourceBucket, sourceKey, targetBucket, targetKey);
        deleteFileFromSourceLocation(sourceBucket, sourceKey);
        log.info("Moved S3 object location FROM bucket {} key {} TO bucket {} key {}", sourceBucket, sourceKey, targetBucket, targetKey);
    }


    public void copyFileToTargetLocation(String sourceBucket, String sourceKey, String targetBucket, String targetKey){
        CopyObjectResult copyObjResult = amazonS3Client.copyObject(sourceBucket, sourceKey, targetBucket, targetKey);
        log.info("Successfully Copied S3 object location TO bucket {} key {} etag {}", targetBucket, targetKey, copyObjResult.getETag());
    }

    public void deleteFileFromSourceLocation(String sourceBucket, String sourceKey){
        amazonS3Client.deleteObject(sourceBucket, sourceKey);
        log.info("Deleted S3 object location FROM bucket {} key {} TO bucket {} key {}", sourceBucket, sourceKey);
    }

}
