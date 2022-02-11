package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.AlertLogService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/alert")
public class AlertLogController {

    private final AlertLogService service;

    @PostMapping
    public AlertLogDto create(@RequestBody final AlertLogDto userLink) throws IsisException {
        return service.create(userLink);
    }

    @PutMapping
    public AlertLogDto update(@RequestBody final AlertLogDto userLink) throws IsisException {
        return service.update(userLink);
    }

    @GetMapping("/{id}")
    public AlertLogDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<AlertLogDto> search(@RequestBody final AlertLogDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<AlertLogDto> pagedSearch(@RequestBody final AlertLogDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
