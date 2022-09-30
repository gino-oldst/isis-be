package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.aws.AwsS3BucketObject;
import st.old.isis.extraction.services.amazon.AmazonS3ClientService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aws-s3")
public class AmazonS3Controller {

    private final AmazonS3ClientService amazonS3ClientService;

    @GetMapping("/list-bucket-files")
    public void listAllRawBucketFiles() {
        amazonS3ClientService.listAllRawBucketFiles();
    }

    @GetMapping("/list-specific-source")
    public void listRawSpecificSource(@RequestParam @NonNull final String source) {
        amazonS3ClientService.listRawSpecificSource(source);
    }

    @GetMapping("/list-specific-date")
    public void listRawSpecificFolder(@RequestParam @NonNull final String marker) {
        amazonS3ClientService.listFilesByBucketAndPrefix(marker);
    }

    @PostMapping("/move-file-to-target-bucket")
    public void moveFileToTargetBucket(@RequestBody AwsS3BucketObject awsS3BucketObject) {
        amazonS3ClientService.moveFileToTargetLocation(awsS3BucketObject.getSourceBucket(),
                awsS3BucketObject.getSourceKey(),
                awsS3BucketObject.getTargetBucket(),
                awsS3BucketObject.getTargetKey());
    }

    @PostMapping("/move-file-to-ingress")
    public void moveFileToIngressBucket(@RequestBody AwsS3BucketObject awsS3BucketObject) {
        amazonS3ClientService.moveFileToIngressBucket(awsS3BucketObject);
    }
}
