package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.storage.services.OpenSearchService;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aws-opensearch")
public class OpenSearchController {

    private final OpenSearchService openSearchService;

    @PostMapping("/index-search")
    public void indexSearch() throws IOException {
//        openSearchService.search();
    }

    @PostMapping("/scroll-search")
    public void scrollSearch() throws IOException {
        openSearchService.scroll();
    }
}
