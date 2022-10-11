package st.old.isis.storage.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import st.old.isis.exception.IsisException;

import java.time.Instant;
@Slf4j
@Component
public class AlertBatchJobExecutionListener implements JobExecutionListener {

    @Autowired private PolicyMapBeanService policyMapServiceBean;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Alert Batch Job Execution has started - {}", Instant.now());
        try {
            policyMapServiceBean.initialize();
        } catch (IsisException e) {
            log.error("An error occurred during policy map bean initialization. {}", e.getMessage());
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Alert Batch Job Execution is complete - {}", Instant.now());
    }

}
