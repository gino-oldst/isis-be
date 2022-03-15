package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.SearchCommunicationDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.SearchCommunicationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/search-communication")
public class SearchCommunicationController {

    private final SearchCommunicationService service;

    @PostMapping("/search")
    public ListDto<CommunicationMessageDto> search(@RequestBody SearchCommunicationDto filter) throws IsisException {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<CommunicationMessageDto> pagedSearch(@RequestBody final SearchCommunicationDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

}
