package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.aws.AwsS3BucketObject;
import st.old.isis.extraction.properties.AwsS3Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3ClientService {

    private final AmazonS3 amazonS3Client;
    private final AwsS3Properties awsS3Properties;

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
