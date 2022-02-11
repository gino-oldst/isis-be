package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.PolicyMapService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/policy-map")
public class PolicyMapController {

    private final PolicyMapService service;

    @PostMapping
    public PolicyMapDto create(@RequestBody final PolicyMapDto user) throws IsisException {
        return service.create(user);
    }

    @PutMapping
    public PolicyMapDto update(@RequestBody final PolicyMapDto user) throws IsisException {
        return service.update(user);
    }

    @GetMapping("/{id}")
    public PolicyMapDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<PolicyMapDto> search(@RequestBody final PolicyMapDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<PolicyMapDto> pagedSearch(@RequestBody final PolicyMapDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
