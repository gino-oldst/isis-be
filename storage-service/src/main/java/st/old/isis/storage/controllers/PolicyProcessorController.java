package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.storage.PolicyProcessorDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.policy.PolicyProcessorService;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/policy-processor")
public class PolicyProcessorController {

    private final PolicyProcessorService service;

    @PostMapping("open-search/policy/range")
    public void processPolicyByDateRangeViaOpenSearch(@RequestBody PolicyProcessorDto dto) throws IsisException, IOException {
        service.processPolicyByDateRangeViaOpenSearch(dto.getPolicyId(), dto.getDateFrom(), dto.getDateTo()); // THREADED
    }

    @PostMapping("open-search/all/pending")
    public void processAllPendingImportDataViaOpenSearch() throws IsisException, IOException {
        service.processAllPendingImportDataViaOpenSearch(); // THREADED
    }

    @PostMapping("open-search/all/import-date")
    public void processAllPendingImportDataViaOpenSearch(@RequestBody PolicyProcessorDto request) throws IsisException, IOException {
        service.processAllPoliciesByImportDateViaOpenSearch(request.getImportDate()); //THREADED
    }

    @PostMapping("open-search/all/filter")
    public void processPolicyIdByFilterViaOpenSearch(@RequestBody PolicyProcessorDto dto) throws IsisException, IOException {
        service.processPolicyIdByFilterViaOpenSearch(dto);
        log.info("processPolicyIdByFilter COMPLETED");
    }

}
