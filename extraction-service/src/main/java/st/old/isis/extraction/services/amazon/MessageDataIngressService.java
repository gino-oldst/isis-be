package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.context.DownloadContext;
import st.old.isis.extraction.properties.AwsS3Properties;
import st.old.isis.extraction.services.BloombergChatExtractionService;
import st.old.isis.extraction.services.internal.StorageService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDataIngressService {

    private final AmazonS3ClientService amazonS3ClientService;
    private final BloombergChatExtractionService bloombergChatExtractionService;
    private final AwsS3Properties awsS3Properties;
    private final StorageService storageService;

    public void ingressAll(@RequestBody DataImportRecordDto request) throws XMLStreamException, JAXBException, IsisException {
        List<S3ObjectSummary> listOfSummaries = amazonS3ClientService.retrieveRawMessageDataFromSource(request.getSource());
        for(S3ObjectSummary os : listOfSummaries) {
            log.info("Processing - {}", os.getKey());
            final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), os.getKey());
            //TODO Will set import date manual for now, replace parse with processXML once scheduled jobs is activated
//            bloombergChatExtractionService.processXml(s3Object.getObjectContent(), request.getSource());
            bloombergChatExtractionService.parse(s3Object.getObjectContent(), request.getImportDate(), request.getSource());
        }
        storageService.merge(DownloadContext.getBatchFilesMap());
    }

    public void ingressFile() throws IsisException {
        ObjectListing objectListing = amazonS3ClientService.listObjects(awsS3Properties.getRawBucket());
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(os.getKey());
            final var s3Object = amazonS3ClientService.downloadS3Object(os.getBucketName(), os.getKey());
            log.info(s3Object.toString());
        }
        storageService.merge(DownloadContext.getBatchFilesMap());
    }


}
