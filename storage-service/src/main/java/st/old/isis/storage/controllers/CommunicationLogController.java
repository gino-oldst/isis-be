package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.CommunicationLogService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/communication")
public class CommunicationLogController {

    private final CommunicationLogService service;

    @GetMapping("/{id}")
    public CommunicationChainDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<CommunicationChainDto> search(@RequestBody final CommunicationChainDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<CommunicationChainDto> pagedSearch(@RequestBody final CommunicationChainDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
