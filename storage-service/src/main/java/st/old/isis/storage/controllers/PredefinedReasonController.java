package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.dto.storage.PredefinedReasonDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.PredefinedReason;
import st.old.isis.storage.services.KeywordService;
import st.old.isis.storage.services.PredefinedReasonService;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/predefined-reason")
public class PredefinedReasonController {

    private final PredefinedReasonService service;

    @PostMapping
    public PredefinedReasonDto create(@RequestBody final PredefinedReasonDto request) throws IsisException {
        return service.create(request);
    }

    @PutMapping
    public PredefinedReasonDto update(@RequestBody final PredefinedReasonDto request) throws IsisException {
        return service.update(request);
    }

    @GetMapping("/{id}")
    public PredefinedReasonDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<PredefinedReasonDto> search(@RequestBody final PredefinedReasonDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<PredefinedReasonDto> pagedSearch(@RequestBody final PredefinedReasonDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }

}
