package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.communication.QuickCommunicationDto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.QuickCommunicationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/quick-communication")
public class QuickCommunicationController {

    private final QuickCommunicationService service;

    @PostMapping("/search/paged")
    public ListDto<QuickCommunicationDto> pagedSearch(@RequestBody final QuickCommunicationDto filter, Pageable pageable) throws IsisException {
        return service.search(filter, pageable);
    }

    @PostMapping("/thread-search/paged")
    public ListDto<QuickCommunicationDto> streamPagedSearch(@RequestBody final QuickCommunicationDto filter, Pageable pageable) throws IsisException {
        return service.pagedFilteredAlerts(filter, pageable);
    }

}
