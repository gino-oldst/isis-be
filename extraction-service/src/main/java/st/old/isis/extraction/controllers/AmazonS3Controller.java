package st.old.isis.extraction.controllers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.amazon.AmazonS3DownloadService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aws-s3")
public class AmazonS3Controller {

    private final AmazonS3 s3Client;
    private final AmazonS3DownloadService amazonS3DownloadService;

    @GetMapping("/list-bucket-files")
    public void listBucketFiles() throws IsisException {

        ObjectListing objectListing = s3Client.listObjects("isis-ii-raw-test");
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(os.getKey());
            final var s3Object = amazonS3DownloadService.downloadS3Object(os.getBucketName(), os.getKey());
            log.info(s3Object.toString());
        }

    }

}
