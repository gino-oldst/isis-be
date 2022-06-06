package st.old.isis.storage.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.BatchAlertNotificationDto;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.policy.PolicyProcessorService;

@Slf4j
@Component
public class MessageItemProcessor implements ItemProcessor<CommunicationMessage, BatchAlertNotificationDto> {

    @Autowired
    private PolicyProcessorService policyProcessorService;

    @Override
    public BatchAlertNotificationDto process(CommunicationMessage message) throws Exception {
        final var convertedAlert = policyProcessorService.processSingleMessageByAllPolicies(message);
        return convertedAlert;
    }

}
