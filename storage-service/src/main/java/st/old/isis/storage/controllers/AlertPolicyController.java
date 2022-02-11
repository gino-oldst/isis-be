package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.AlertPolicyService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/alert-policy")
public class AlertPolicyController {

    private final AlertPolicyService service;

    @PostMapping
    public AlertPolicyDto create(@RequestBody final AlertPolicyDto alert) throws IsisException{
        return service.create(alert);
    }

    @PutMapping
    public AlertPolicyDto update(@RequestBody final AlertPolicyDto alert) throws IsisException{
        return service.update(alert);
    }

    @GetMapping("/{id}")
    public AlertPolicyDto getById(@PathVariable final String id) throws IsisException{
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<AlertPolicyDto> search(@RequestBody final AlertPolicyDto filter) { return service.search(filter); }

    @PostMapping("/search/paged")
    public ListDto<AlertPolicyDto> pagedSearch(@RequestBody final AlertPolicyDto filter, Pageable pageable){
        return service.search(filter, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
