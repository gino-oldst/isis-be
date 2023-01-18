package st.old.isis.storage.filters.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.policy.PolicyProcessorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyProcessorScheduler {

    private final PolicyProcessorService policyProcessorService;

    @Scheduled(cron = "${isis.scheduler.policy-processor.all-pending.cron}")
    @SchedulerLock(name = "PolicyProcessorScheduler_processAllPendingImportData",
            lockAtLeastFor = "${isis.scheduler.policy-processor.all-pending.lock-time}")
    public void processAllPendingImportData() throws IsisException {
        log.info("SCHEDULED - Processing Policies for All Pending Import Dates...");
        policyProcessorService.processAllPendingImportData();
    }

}
