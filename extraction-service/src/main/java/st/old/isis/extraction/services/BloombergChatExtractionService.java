package st.old.isis.extraction.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.Dto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.entities.generated.Conversation;
import st.old.isis.entities.generated.FileDump;
import st.old.isis.entities.generated.Message;
import st.old.isis.enums.SourceOrigin;
import st.old.isis.enums.SourceType;
import st.old.isis.extraction.events.publisher.EventPublisher;
import st.old.isis.extraction.services.internal.NotificationService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BloombergChatExtractionService extends XmlExtractionService {

  private final EventPublisher eventPublisher;
  private final Unmarshaller unmarshaller;

  public BloombergChatExtractionService(
      @NonNull final NotificationService notificationService,
      @NonNull final EventPublisher eventPublisher)
      throws JAXBException {
    super(notificationService);
    this.eventPublisher = eventPublisher;

    final var jaxbContext = JAXBContext.newInstance(FileDump.class);
    this.unmarshaller = jaxbContext.createUnmarshaller();
  }

  @Override
  protected void processReader(
      @NonNull final XMLStreamReader reader, @NonNull final LocalDate importDate)
      throws JAXBException {

    if (reader.getName().getLocalPart().equals("Conversation")) {
      final var conversation = (Conversation) unmarshaller.unmarshal(reader);

      Optional.ofNullable(parseConversation(conversation, importDate))
          .ifPresent(eventPublisher::addToCommunicationChainBatch);
    }
  }

  @Override
  protected void finalizeParsing() {
    eventPublisher.finalizeBatchCommunication(true);
    eventPublisher.finalizeBatchExternalUsers(true);
  }

  @Audited(event = "BLOOMBERG_PARSE_XML_STREAM")
  public void processXml(@NonNull final InputStream inputStream)
      throws JAXBException, XMLStreamException {
    try {
      parse(inputStream, LocalDate.now());
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  @Audited(event = "BLOOMBERG_PARSE_XML_MULTIPART")
  public void processXml(MultipartFile multipartFile, LocalDate importDate)
      throws IOException, JAXBException, XMLStreamException {
    try {
      parse(multipartFile.getInputStream(), importDate);
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  private CommunicationChainDto parseConversation(
      @NonNull final Conversation conversation, @NonNull final LocalDate importDate) {

    final var roomId = conversation.getRoomID();

    final var mappedMessages = parseMessages(conversation.getMessage());

    final var communications =
        mappedMessages.get("communication").stream()
            .map(message -> (CommunicationMessageDto) message)
            .collect(Collectors.toList());

    final var recipients =
        new ArrayList<>(
            mappedMessages.get("recipient").stream()
                .map(recipient -> (CommunicationRecipientDto) recipient)
                .collect(
                    Collectors.toMap(
                        CommunicationRecipientDto::getRecipient,
                        Function.identity(),
                        (v1, v2) -> v1))
                .values());

    final var users =
        mappedMessages.get("user").stream()
            .map(user -> (ExternalUserDto) user)
            .collect(Collectors.toList());

    eventPublisher.addToExternalUserBatch(users);

    final var timeStarted =
        LocalDateTime.ofInstant(
                Instant.ofEpochSecond(conversation.getStartTimeUTC()), ZoneOffset.UTC)
            .atZone(Clock.systemDefaultZone().getZone())
            .toLocalDateTime();

    final var timeEnded =
        LocalDateTime.ofInstant(Instant.ofEpochSecond(conversation.getEndTimeUTC()), ZoneOffset.UTC)
            .atZone(Clock.systemDefaultZone().getZone())
            .toLocalDateTime();

    return CommunicationChainDto.builder()
        .source(SourceType.BLOOMBERG.toString())
        .sourceChainId(roomId)
        .dateFrom(timeStarted)
        .dateTo(timeEnded)
        .dateImported(importDate)
        .messages(communications)
        .recipients(recipients)
        .build();
  }

  private Map<String, List<? extends Dto>> parseMessages(@NonNull final List<Message> messages) {

    final var communications = new ArrayList<CommunicationMessageDto>();
    final var recipients = new ArrayList<CommunicationRecipientDto>();
    final var users = new ArrayList<ExternalUserDto>();

    for (final var message : messages) {

      final var timeSent =
          LocalDateTime.ofInstant(Instant.ofEpochSecond(message.getDateTimeUTC()), ZoneOffset.UTC)
              .atZone(Clock.systemUTC().getZone())
              .toLocalDateTime();

      final var user = message.getUser();

      final var chainLinkId = message.getConversationID();

      final var communication =
          CommunicationMessageDto.builder()
              .message(message.getContent())
              .timeSent(timeSent)
              .chainLinkId(chainLinkId)
              .sender(user.getEmailAddress())
              .build();

      final var recipient =
          CommunicationRecipientDto.builder()
              .chainLinkId(chainLinkId)
              .recipient(user.getEmailAddress())
              .build();

      final var externalUser =
          ExternalUserDto.builder()
              .externalId(user.getEmailAddress())
              .email(user.getEmailAddress())
              .firstName(user.getFirstName())
              .lastName(user.getLastName())
              .source(SourceOrigin.BLOOMBERG_CHAT.toString())
              .build();

      communications.add(communication);
      recipients.add(recipient);
      users.add(externalUser);
    }

    return Map.of("communication", communications, "recipient", recipients, "user", users);
  }
}
