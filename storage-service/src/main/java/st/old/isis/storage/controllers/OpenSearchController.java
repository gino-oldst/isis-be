package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.storage.services.OpenSearchService;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/opensearch-get")
public class OpenSearchController {

    private final OpenSearchService openSearchService;

    @PostMapping("/request-single")
    public GetResponse indexSingleMessage(@RequestBody CommunicationMessageDto communicationMessageDto) throws IOException {
        return openSearchService.search();
    }
}
