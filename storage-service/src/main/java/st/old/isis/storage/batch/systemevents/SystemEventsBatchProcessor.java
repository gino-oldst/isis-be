package st.old.isis.storage.batch.systemevents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.*;
import st.old.isis.enums.SourceType;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.policy.PolicyProcessorService;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.utils.BatchJobUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemEventsBatchProcessor implements ItemProcessor<CommunicationMessage, CommunicationMessageDto>  {

    @Autowired private CommunicationMessageService communicationMessageService;
    private List<PatternBypassDto> patternBypassList;
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
        stepExecution.getJobExecution().getExecutionContext().remove(BatchJobUtils.EXECUTION_CONTEXT_KEY_PATTERN_BYPASS);
    }
    @Override
    public CommunicationMessageDto process(CommunicationMessage message) throws Exception {

        final var executionContext= stepExecution.getJobExecution().getExecutionContext();

        patternBypassList= (List<PatternBypassDto>) executionContext.get(BatchJobUtils.EXECUTION_CONTEXT_KEY_PATTERN_BYPASS);

        if(!message.getSource().equals(SourceType.BLOOMBERG_CHAT.toString())
                && !message.getSource().equals(SourceType.REUTERS.toString())) return null;

        final var convertedAlert = communicationMessageService.processIfSystemEvent(patternBypassList, message);

        return convertedAlert;
    }

}
