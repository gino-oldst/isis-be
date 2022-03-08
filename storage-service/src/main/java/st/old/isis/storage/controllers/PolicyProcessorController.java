package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.PolicyProcessorDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.policy.PolicyProcessorService;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/policy-processor")
public class PolicyProcessorController {

    private final PolicyProcessorService service;

    @PostMapping("/policy/range")
    public void processPolicyByDateRange(@RequestBody PolicyProcessorDto dto) throws IsisException {
        service.processPolicyByDateRange(dto.getPolicyId(), dto.getDateFrom(), dto.getDateTo());
    }

    @PostMapping("/all/pending")
    public void processAllPendingImportData() throws IsisException {
        service.processAllPendingImportData();
    }

    @PostMapping("/all/import-date")
    public void processAllPoliciesByImportDate(@RequestBody PolicyProcessorDto request) throws IsisException {
        service.processAllPoliciesByImportDate(request.getImportDate());
    }

}
