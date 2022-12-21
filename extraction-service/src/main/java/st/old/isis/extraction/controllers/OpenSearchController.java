package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.index.IndexResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.aws.AwsS3BucketObject;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.extraction.services.OpenSearchService;
import st.old.isis.extraction.services.amazon.AmazonS3ClientService;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aws-opensearch")
public class OpenSearchController {

    private final OpenSearchService openSearchService;

    @PostMapping("/index-single")
    public IndexResponse indexSingleMessage(@RequestBody CommunicationMessageDto communicationMessageDto) throws IOException {
        return openSearchService.indexSingleMessage(communicationMessageDto);
    }

}
