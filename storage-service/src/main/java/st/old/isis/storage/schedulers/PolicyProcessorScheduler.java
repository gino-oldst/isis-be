package st.old.isis.storage.schedulers;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PolicyProcessorScheduler {

    @Scheduled(cron = "* * * ? * *")
    @SchedulerLock(name = "PolicyProcessorScheduler_processAllPendingImportData")
    public void processAllPendingImportData() {
        log.info("SCHEDULED - Processing Policies for All Pending Import Date...");
    }

}
