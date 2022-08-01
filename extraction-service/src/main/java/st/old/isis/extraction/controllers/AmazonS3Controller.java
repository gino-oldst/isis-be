package st.old.isis.extraction.controllers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.aws.AwsS3BucketObject;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.BloombergChatExtractionService;
import st.old.isis.extraction.services.amazon.AmazonS3ClientService;
import st.old.isis.extraction.services.amazon.MessageDataIngressService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aws-s3")
public class AmazonS3Controller {

    private final AmazonS3ClientService amazonS3ClientService;
    private final MessageDataIngressService messageDataIngressService;

    @GetMapping("/list-bucket-files")
    public void listAllRawBucketFiles() {
        amazonS3ClientService.listAllRawBucketFiles();
    }

    @PostMapping("/ingress-all")
    public void ingressAll(@RequestBody DataImportRecordDto request) throws XMLStreamException, JAXBException {
        messageDataIngressService.ingressAll(request);
    }

    @PostMapping("/ingress-file")
    public void ingressFile() throws IsisException {
        messageDataIngressService.ingressFile();
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
