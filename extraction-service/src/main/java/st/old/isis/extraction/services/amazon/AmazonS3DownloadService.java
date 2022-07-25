package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.extraction.AwsS3Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3DownloadService {

    private final AmazonS3 amazonS3Client;

    public S3Object downloadS3Object(String bucketName, String objectName){
        S3Object s3object = amazonS3Client.getObject(bucketName, objectName);
        log.info("Downloaded s3 object", s3object.toString());
        return s3object;
    }

}
