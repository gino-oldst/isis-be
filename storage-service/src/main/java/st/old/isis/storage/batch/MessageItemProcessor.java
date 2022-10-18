package st.old.isis.storage.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.BatchAlertNotificationDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.policy.PolicyProcessorService;
import st.old.isis.storage.utils.BatchJobUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageItemProcessor implements ItemProcessor<CommunicationMessage, BatchAlertNotificationDto>  {

    @Autowired private PolicyProcessorService policyProcessorService;
    private Map<AlertPolicyDto, List<KeywordDto>> policyMap;
    private StepExecution stepExecution;
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        log.info("Called beforeStep().");
        this.stepExecution = stepExecution;
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        log.info("Called afterStep().");
        stepExecution.getJobExecution().getExecutionContext().remove(BatchJobUtils.EXECUTION_CONTEXT_KEY_POLICY_MAP);
    }
    @Override
    public BatchAlertNotificationDto process(CommunicationMessage message) throws Exception {

        final var executionContext= stepExecution.getJobExecution().getExecutionContext();

        if(!executionContext.containsKey(BatchJobUtils.EXECUTION_CONTEXT_KEY_POLICY_MAP)){
            throw new IsisException("Policy Map was not found in Step Execution Context.");
        }
        policyMap = (Map<AlertPolicyDto, List<KeywordDto>>) executionContext.get(BatchJobUtils.EXECUTION_CONTEXT_KEY_POLICY_MAP);

        final var convertedAlert = policyProcessorService.processSingleMessageByAllPolicies(policyMap, message);
        return convertedAlert;
    }

}
