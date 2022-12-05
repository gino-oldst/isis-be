package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.UserLinkService;

import java.io.IOException;
import java.time.LocalDate;

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

    @PostMapping("/upload-user-link")
    public ListDto<UserLinkDto> uploadProfiles(@RequestPart MultipartFile file) throws IsisException, IOException {
        return service.upload(file);
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
