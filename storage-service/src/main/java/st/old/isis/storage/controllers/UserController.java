package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.UserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService service;

    @PostMapping
    public UserDto create(@RequestBody final UserDto user) {
        return service.create(user);
    }

    @PutMapping
    public UserDto update(@RequestBody final UserDto user) throws IsisException {
        return service.update(user);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<UserDto> search(@RequestBody final UserDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<UserDto> pagedSearch(@RequestBody final UserDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
