package st.old.isis.extraction.events.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.extraction.events.BatchCommunicationChainEvent;
import st.old.isis.extraction.events.BatchCommunicationMessageEvent;
import st.old.isis.extraction.events.BatchCommunicationRecipientEvent;
import st.old.isis.extraction.events.BatchExternalUsersEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public void addToCommunicationChainBatch(
      @NonNull final CommunicationChainDto communicationChain) {

    final var batchCommunicationChainEvent =
        new BatchCommunicationChainEvent(this, communicationChain);

    applicationEventPublisher.publishEvent(batchCommunicationChainEvent);
  }

  public void addToExternalUserBatch(@NonNull final List<ExternalUserDto> users) {

    final var batchExternalUsersEvent = new BatchExternalUsersEvent(this, users);

    applicationEventPublisher.publishEvent(batchExternalUsersEvent);
  }

  public void addToMessageBatch(@NonNull final List<CommunicationMessageDto> messages) {

    final var batchMessagesEvent = new BatchCommunicationMessageEvent(this, messages);

    applicationEventPublisher.publishEvent(batchMessagesEvent);
  }

  public void addToRecipientBatch(@NonNull final List<CommunicationRecipientDto> recipients) {

    final var batchRecipientsEvent = new BatchCommunicationRecipientEvent(this, recipients);

    applicationEventPublisher.publishEvent(batchRecipientsEvent);
  }

  public void finalizeBatchCommunication(@NonNull final boolean finalize, String fileSource) {

    final var finalizeParseEvent = new BatchCommunicationChainEvent(this, finalize, fileSource);

    applicationEventPublisher.publishEvent(finalizeParseEvent);
  }

  public void finalizeBatchExternalUsers(@NonNull final boolean finalize, String fileSource) {

    final var finalizeParseEvent = new BatchExternalUsersEvent(this, finalize, fileSource);

    applicationEventPublisher.publishEvent(finalizeParseEvent);
  }

  public void finalizeBatchMessages(@NonNull final boolean finalize, String fileSource) {

    final var finalizeParseEvent = new BatchCommunicationMessageEvent(this, finalize, fileSource);

    applicationEventPublisher.publishEvent(finalizeParseEvent);
  }

  public void finalizeBatchRecipients(@NonNull final boolean finalize, String fileSource) {

    final var finalizeParseEvent = new BatchCommunicationRecipientEvent(this, finalize, fileSource);

    applicationEventPublisher.publishEvent(finalizeParseEvent);
  }
}
