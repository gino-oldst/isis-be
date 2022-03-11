package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.CommunicationRecipientService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/recipient")
public class CommunicationRecipientController {

    private final CommunicationRecipientService service;

    @GetMapping("/{id}")
    public CommunicationRecipientDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<CommunicationRecipientDto> search(@RequestBody final CommunicationRecipientDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<CommunicationRecipientDto> pagedSearch(@RequestBody final CommunicationRecipientDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
