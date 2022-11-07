package st.old.isis.storage.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.dto.storage.PatternBypassDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.PatternBypassService;
import st.old.isis.storage.utils.BatchJobUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@StepScope
public class AlertBatchStepExecutionListener extends StepExecutionListenerSupport {

    @Autowired private PolicyMapBeanService policyMapServiceBean;

    @Autowired private PatternBypassService patternBypassServiceBean;

    @Override
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        log.info("Alert Batch Step Execution has started - {}", Instant.now());

        log.info("beforeStep - Generating policy map for steps");
        Map<AlertPolicyDto, List<KeywordDto>> policyMap = null;
        try {
            policyMap = policyMapServiceBean.initialize();
        } catch (IsisException e) {
            log.error("An error occurred while generating policy map for step execution. {}", e.getMessage());
        }

        log.info("beforeStep - Generating pattern bypass map for steps");
        List<PatternBypassDto> patternBypassList =  patternBypassServiceBean.findWithFilter(PatternBypassDto.builder().build()).getData();

        stepExecution.getJobExecution().getExecutionContext().put(BatchJobUtils.EXECUTION_CONTEXT_KEY_POLICY_MAP, policyMap);
        stepExecution.getJobExecution().getExecutionContext().put(BatchJobUtils.EXECUTION_CONTEXT_KEY_PATTERN_BYPASS, patternBypassList);
    }

}
