package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.KeywordService;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/keyword")
public class KeywordController {

    private final KeywordService service;

    @PostMapping
    public KeywordDto create(@RequestBody final KeywordDto user) throws IsisException {
        return service.create(user);
    }

    @PutMapping
    public KeywordDto update(@RequestBody final KeywordDto user) throws IsisException {
        return service.update(user);
    }

    @GetMapping("/{id}")
    public KeywordDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<KeywordDto> search(@RequestBody final KeywordDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<KeywordDto> pagedSearch(@RequestBody final KeywordDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }

    @PostMapping("/upload-keyword")
    public ResponseEntity<?> uploadKeywordList(@RequestPart MultipartFile file, @RequestPart String policyId) throws IOException, IsisException {
            service.uploadKeywordListViaApi(file, policyId);
            return new ResponseEntity<String>("success", HttpStatus.valueOf(200));
    }
}
