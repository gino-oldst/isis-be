package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.CommunicationMessageService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/message")
public class CommunicationMessageController {

    private final CommunicationMessageService service;

    @PostMapping("/batch")
    public List<CommunicationMessageDto> batch(@RequestBody final List<CommunicationMessageDto> messages) {
        return service.batchSave(messages);
    }

    @GetMapping("/{id}")
    public CommunicationMessageDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<CommunicationMessageDto> search(@RequestBody final CommunicationMessageDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<CommunicationMessageDto> pagedSearch(@RequestBody final CommunicationMessageDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
