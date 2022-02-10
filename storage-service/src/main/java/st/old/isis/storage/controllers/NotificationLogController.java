package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.NotificationLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.NotificationLogService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationLogController {

    private final NotificationLogService service;

    @PostMapping
    public NotificationLogDto create(@RequestBody final NotificationLogDto user) throws IsisException {
        return service.create(user);
    }

    @PutMapping
    public NotificationLogDto update(@RequestBody final NotificationLogDto user) throws IsisException {
        return service.update(user);
    }

    @GetMapping("/{id}")
    public NotificationLogDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<NotificationLogDto> search(@RequestBody final NotificationLogDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<NotificationLogDto> pagedSearch(@RequestBody final NotificationLogDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
