package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.UserLinkService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user-link")
public class UserLinkController {

    private final UserLinkService service;

    @PostMapping
    public UserLinkDto create(@RequestBody final UserLinkDto userLink) throws IsisException {
        return service.create(userLink);
    }

    @PutMapping
    public UserLinkDto update(@RequestBody final UserLinkDto userLink) throws IsisException {
        return service.update(userLink);
    }

    @GetMapping("/{id}")
    public UserLinkDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<UserLinkDto> search(@RequestBody final UserLinkDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<UserLinkDto> pagedSearch(@RequestBody final UserLinkDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
