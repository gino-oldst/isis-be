package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.extraction.services.OpenSearchService;

import java.io.IOException;
import java.util.List;

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

    @PostMapping("/index-bulk")
    public BulkResponse indexBulk(@RequestBody List<CommunicationMessageDto> messages) throws IOException {
        return openSearchService.indexBatch(messages);
    }
}
