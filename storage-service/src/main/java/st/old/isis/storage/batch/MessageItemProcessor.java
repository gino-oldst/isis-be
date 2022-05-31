package st.old.isis.storage.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.entities.CommunicationMessage;

@Slf4j
@Component
public class MessageItemProcessor implements ItemProcessor<CommunicationMessage, AlertLog> {

    @Override
    public AlertLog process(CommunicationMessage message) throws Exception {

        log.info("Processing message id {} from sender {}", message.getId(), message.getSender());

        // Do policy process hear
        final var convertedAlert = AlertLog.builder().commMessageId(message.getId()).build();

        return convertedAlert;
    }

}
