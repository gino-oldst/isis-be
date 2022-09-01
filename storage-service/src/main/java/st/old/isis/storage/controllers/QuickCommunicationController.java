package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.communication.QuickCommunicationDto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.RecentMessageDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.services.QuickCommunicationService;

import java.util.List;

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
        log.info("STARTED: streamPagedSearch");
        return service.getFilteredAlerts(filter, pageable);
    }

}
