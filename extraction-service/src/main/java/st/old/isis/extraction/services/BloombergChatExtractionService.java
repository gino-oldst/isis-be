package st.old.isis.extraction.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.Dto;
import st.old.isis.dto.metadata.BloombergAttachment;
import st.old.isis.dto.metadata.BloombergChatAttachmentMetadata;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.enums.MessageType;
import st.old.isis.enums.Source;
import st.old.isis.enums.SourceType;
import st.old.isis.extraction.entities.xml.*;
import st.old.isis.extraction.events.publisher.EventPublisher;
import st.old.isis.extraction.services.internal.NotificationService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    final var jaxbContext = JAXBContext.newInstance(BloombergChat.class);
    this.unmarshaller = jaxbContext.createUnmarshaller();
  }

  @Override
  protected void processReader(
      @NonNull final XMLStreamReader reader,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource)
      throws JAXBException {

    if (reader.getName().getLocalPart().equals("Conversation")) {
      final var conversation = unmarshaller.unmarshal(reader, BloombergChatConversation.class);

      Optional.ofNullable(parseConversation(conversation.getValue(), importDate, fileSource))
          .ifPresent(eventPublisher::addToCommunicationChainBatch);
    }
  }

  @Override
  protected void finalizeParsing(String fileSource) {
    eventPublisher.finalizeBatchCommunication(true, fileSource);
    eventPublisher.finalizeBatchExternalUsers(true, fileSource);
    eventPublisher.finalizeBatchMessages(true, fileSource);
    eventPublisher.finalizeBatchRecipients(true, fileSource);
  }

  @Audited(event = "BLOOMBERG_PARSE_CHAT_XML_STREAM")
  public void processXml(@NonNull final InputStream inputStream, String fileSource)
      throws JAXBException, XMLStreamException {
    try {
      parse(inputStream, LocalDate.now(), fileSource);
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  @Audited(event = "BLOOMBERG_PARSE_CHAT_XML_MULTIPART")
  public void processXml(MultipartFile multipartFile, LocalDate importDate)
      throws IOException, JAXBException, XMLStreamException {
    try {
      parse(
          new BufferedInputStream(multipartFile.getInputStream()),
          importDate,
          multipartFile.getName());
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  private CommunicationChainDto parseConversation(
      @NonNull final BloombergChatConversation conversation,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource) {

    final var roomId = conversation.getRoomID();

    final var mappedMessages = parseMessages(conversation.getMessage(), importDate, fileSource);
    final var attachmentMessages =
        parseAttachments(conversation.getAttachment(), importDate, fileSource);

    final var mappedMessagesCommunications = mappedMessages.get("communication");
    final var mappedAttachmentCommunications = attachmentMessages.get("communication");

    final var communications =
        Stream.concat(
                mappedMessagesCommunications.stream(), mappedAttachmentCommunications.stream())
            .map(message -> (CommunicationMessageDto) message)
            .collect(Collectors.toList());

    final var mappedEntryEvents =
        parseEntryEvents(conversation.getParticipantEntered(), fileSource);

    final var mappedMessagesRecipients = mappedMessages.get("recipient");
    final var mappedEntryEventsRecipients = mappedEntryEvents.get("recipient");
    final var mappedAttachmentRecipients = attachmentMessages.get("recipient");

    // concat list of recipients and remove duplicates
    final var recipients =
        new ArrayList<>(
            Stream.of(
                    mappedMessagesRecipients.stream(),
                    mappedEntryEventsRecipients.stream(),
                    mappedAttachmentRecipients.stream())
                .flatMap(stream -> stream)
                .map(recipient -> (CommunicationRecipientDto) recipient)
                .collect(
                    Collectors.toMap(
                        CommunicationRecipientDto::getRecipient,
                        Function.identity(),
                        (v1, v2) -> v1))
                .values());

    final var mappedMessagesUsers = mappedMessages.get("user");
    final var mappedEntryEventsUsers = mappedEntryEvents.get("user");
    final var mappedAttachmentUsers = attachmentMessages.get("user");

    // concat list of users and remove duplicates
    final var users =
        new ArrayList<>(
            Stream.of(
                    mappedMessagesUsers.stream(),
                    mappedEntryEventsUsers.stream(),
                    mappedAttachmentUsers.stream())
                .flatMap(stream -> stream)
                .map(user -> (ExternalUserDto) user)
                .collect(
                    Collectors.toMap(
                        ExternalUserDto::getExternalId, Function.identity(), (v1, v2) -> v1))
                .values());

    eventPublisher.addToExternalUserBatch(users);
    eventPublisher.addToMessageBatch(communications);
    eventPublisher.addToRecipientBatch(recipients);

    final var timeStarted =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(conversation.getStartTimeUTC()), ZoneId.systemDefault());

    final var timeEnded =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(conversation.getEndTimeUTC()), ZoneId.systemDefault());

    return CommunicationChainDto.builder()
        .source(SourceType.BLOOMBERG_CHAT.toString())
        .sourceChainId(roomId)
        .dateFrom(timeStarted)
        .dateTo(timeEnded)
        .dateImported(importDate)
        .fileSource(fileSource)
        .build();
  }

  private Map<String, List<? extends Dto>> parseMessages(
      @NonNull final List<BloombergChatMessage> messages,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource) {

    final var communications = new ArrayList<CommunicationMessageDto>();
    final var recipients = new ArrayList<CommunicationRecipientDto>();
    final var users = new ArrayList<ExternalUserDto>();

    for (final var message : messages) {

      final var timeSent =
          LocalDateTime.ofInstant(
              Instant.ofEpochSecond(message.getDateTimeUTC()), ZoneId.systemDefault());

      final var user = message.getUser();

      final var chainLinkId = message.getConversationID();

      final var communication =
          CommunicationMessageDto.builder()
              .importDate(importDate)
              .message(message.getContent())
              .timeSent(timeSent)
              .chainLinkId(chainLinkId)
              .sender(user.getEmailAddress())
              .source(SourceType.BLOOMBERG_CHAT.toString())
              .fileSource(fileSource)
              .build();

      final var recipient =
          CommunicationRecipientDto.builder()
              .chainLinkId(chainLinkId)
              .recipient(user.getEmailAddress())
              .dateImported(importDate)
              .fileSource(fileSource)
              .build();

      final var externalUser =
          ExternalUserDto.builder()
              .externalId(user.getEmailAddress())
              .email(user.getEmailAddress())
              .firstName(user.getFirstName())
              .lastName(user.getLastName())
              .source(Source.BLOOMBERG.toString())
              .fileSource(fileSource)
              .build();

      communications.add(communication);
      recipients.add(recipient);
      users.add(externalUser);
    }

    return Map.of("communication", communications, "recipient", recipients, "user", users);
  }

  private Map<String, List<? extends Dto>> parseAttachments(
      @NonNull final List<BloombergChatAttachment> attachments,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource) {

    final var communications = new ArrayList<CommunicationMessageDto>();
    final var recipients = new ArrayList<CommunicationRecipientDto>();
    final var users = new ArrayList<ExternalUserDto>();

    for (final var attachment : attachments) {

      final var timeSent =
          LocalDateTime.ofInstant(
              Instant.ofEpochSecond(attachment.getDateTimeUTC()), ZoneId.systemDefault());

      final var user = attachment.getUser();

      final var chainLinkId = attachment.getConversationID();

      final var communication =
          CommunicationMessageDto.builder()
              .importDate(importDate)
              .timeSent(timeSent)
              .chainLinkId(chainLinkId)
              .sender(user.getEmailAddress())
              .source(SourceType.BLOOMBERG_CHAT.toString())
              .type(MessageType.BLOOMBERG_CHAT_ATTACHMENT.toString())
              .metadata(
                  BloombergChatAttachmentMetadata.builder()
                      .attachment(
                          BloombergAttachment.builder()
                              .fileId(attachment.getFileID())
                              .filename(attachment.getFileName())
                              .fileSize(Long.toString(attachment.getFileSize()))
                              .build())
                      .build())
              .fileSource(fileSource)
              .build();

      final var recipient =
          CommunicationRecipientDto.builder()
              .chainLinkId(chainLinkId)
              .recipient(user.getEmailAddress())
              .dateImported(importDate)
              .fileSource(fileSource)
              .build();

      final var externalUser =
          ExternalUserDto.builder()
              .externalId(user.getEmailAddress())
              .email(user.getEmailAddress())
              .firstName(user.getFirstName())
              .lastName(user.getLastName())
              .source(Source.BLOOMBERG.toString())
              .fileSource(fileSource)
              .build();

      communications.add(communication);
      recipients.add(recipient);
      users.add(externalUser);
    }

    return Map.of("communication", communications, "recipient", recipients, "user", users);
  }

  private Map<String, List<? extends Dto>> parseEntryEvents(
      @NonNull final List<BloombergChatParticipantEntered> entryEvents,
      @NonNull final String fileSource) {

    final var recipients = new ArrayList<CommunicationRecipientDto>();
    final var users = new ArrayList<ExternalUserDto>();

    for (final var entryEvent : entryEvents) {

      final var user = entryEvent.getUser();

      final var chainLinkId = entryEvent.getConversationID();

      final var recipient =
          CommunicationRecipientDto.builder()
              .chainLinkId(chainLinkId)
              .recipient(user.getEmailAddress())
              .fileSource(fileSource)
              .build();

      final var externalUser =
          ExternalUserDto.builder()
              .externalId(user.getEmailAddress())
              .email(user.getEmailAddress())
              .firstName(user.getFirstName())
              .lastName(user.getLastName())
              .source(Source.BLOOMBERG.toString())
              .fileSource(fileSource)
              .build();

      recipients.add(recipient);
      users.add(externalUser);
    }

    return Map.of("recipient", recipients, "user", users);
  }
}
