package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.extraction.properties.AwsS3Properties;
import st.old.isis.extraction.services.BloombergChatExtractionService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDataIngressService {

    private final AmazonS3ClientService amazonS3ClientService;
    private final BloombergChatExtractionService bloombergChatExtractionService;
    private final AwsS3Properties awsS3Properties;

    public void ingressAll(@RequestBody DataImportRecordDto request) throws XMLStreamException, JAXBException {
        ObjectListing objectListing = amazonS3ClientService.listObjects(awsS3Properties.getRawBucket());
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), os.getKey());
            bloombergChatExtractionService.processXml(s3Object.getObjectContent(), null);
        }
    }

    public void ingressFile() {
        ObjectListing objectListing = amazonS3ClientService.listObjects(awsS3Properties.getRawBucket());
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(os.getKey());
            final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), os.getKey());
            log.info(s3Object.toString());
        }
    }


}
