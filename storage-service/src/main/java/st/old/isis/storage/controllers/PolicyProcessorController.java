package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.policy.PolicyProcessorService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/policy-processor")
public class PolicyProcessorController {

    private final PolicyProcessorService service;

    @PostMapping
    public void processPolicies() throws IsisException {
        service.processAllPendingImportData();
    }

}
