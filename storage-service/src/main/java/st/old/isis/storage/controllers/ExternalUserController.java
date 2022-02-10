package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.ExternalUserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/external")
public class ExternalUserController {

    private final ExternalUserService service;

    @PostMapping("/batch")
    public ListDto<ExternalUserDto> batchedSave(@RequestBody List<ExternalUserDto> users) {
        return service.batchedSave(users);
    }

    @PostMapping("/search")
    public ListDto<ExternalUserDto> search(@RequestBody final ExternalUserDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<ExternalUserDto> pagedSearch(@RequestBody final ExternalUserDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
