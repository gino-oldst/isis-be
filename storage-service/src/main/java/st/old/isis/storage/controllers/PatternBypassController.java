package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.PatternBypassDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.PatternBypassService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pattern-bypass")
public class PatternBypassController {

    private final PatternBypassService service;

    @PostMapping
    public PatternBypassDto create(@RequestBody final PatternBypassDto request) throws IsisException {
        return service.createRecord(request);
    }

    @PutMapping
    public PatternBypassDto update(@RequestBody final PatternBypassDto request) throws IsisException {
        return service.updateRecord(request);
    }

    @GetMapping("/{id}")
    public PatternBypassDto getById(@PathVariable final String id) throws IsisException {
        return service.getRecordById(id);
    }

    @PostMapping("/search")
    public ListDto<PatternBypassDto> search(@RequestBody final PatternBypassDto filter) {
        return service.findWithFilter(filter);
    }

}
