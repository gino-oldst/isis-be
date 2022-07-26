package st.old.isis.extraction.controllers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.azure.core.annotation.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.BloombergChatExtractionService;
import st.old.isis.extraction.services.amazon.AmazonS3DownloadService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aws-s3")
public class AmazonS3Controller {

    private final AmazonS3 s3Client;
    private final AmazonS3DownloadService amazonS3DownloadService;
    private final BloombergChatExtractionService chatExtractionService;

    @GetMapping("/list-bucket-files")
    public void listBucketFiles() throws IsisException {

        ObjectListing objectListing = s3Client.listObjects("isis-ii-raw-test");
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(os.getKey());
            final var s3Object = amazonS3DownloadService.downloadS3Object(os.getBucketName(), os.getKey());
            log.info(s3Object.toString());
        }

    }

    @PostMapping("/ingress-all")
    public void ingressAll(@RequestBody DataImportRecordDto request) throws IsisException, XMLStreamException, JAXBException {

        ObjectListing objectListing = s3Client.listObjects("isis-ii-raw-test");
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            final var s3Object = amazonS3DownloadService.downloadS3Object(os.getBucketName(), os.getKey());
            chatExtractionService.processXml(s3Object.getObjectContent(), null);
        }

    }

    @PostMapping("/ingress-file")
    public void ingressFile() throws IsisException {

        ObjectListing objectListing = s3Client.listObjects("isis-ii-raw-test");
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(os.getKey());
            final var s3Object = amazonS3DownloadService.downloadS3Object(os.getBucketName(), os.getKey());
            log.info(s3Object.toString());
        }

    }





}
