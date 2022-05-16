package st.old.isis.extraction.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.metadata.*;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.enums.MessageType;
import st.old.isis.enums.Source;
import st.old.isis.enums.SourceType;
import st.old.isis.extraction.entities.xml.BloombergEmail;
import st.old.isis.extraction.entities.xml.BloombergEmailMessage;
import st.old.isis.extraction.entities.xml.BloombergEmailRecipient;
import st.old.isis.extraction.entities.xml.BloombergEmailUserInfo;
import st.old.isis.extraction.events.publisher.EventPublisher;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.utils.ListUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BloombergEmailExtractionService extends XmlExtractionService {

  private final EventPublisher eventPublisher;
  private final Unmarshaller unmarshaller;

  public BloombergEmailExtractionService(
      @NonNull final NotificationService notificationService,
      @NonNull final EventPublisher eventPublisher)
      throws JAXBException {
    super(notificationService);
    this.eventPublisher = eventPublisher;

    final var jaxbContext = JAXBContext.newInstance(BloombergEmail.class);
    this.unmarshaller = jaxbContext.createUnmarshaller();
  }

  @Override
  protected void processReader(
      @NonNull final XMLStreamReader reader,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource)
      throws JAXBException {

    if (reader.getName().getLocalPart().equals("Message")) {
      final var conversation = unmarshaller.unmarshal(reader, BloombergEmailMessage.class);

      Optional.ofNullable(parseMessage(conversation.getValue(), importDate, fileSource))
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

  @Audited(event = "BLOOMBERG_PARSE_EMAIL_XML_STREAM")
  public void processXml(@NonNull final InputStream inputStream, String fileSource)
      throws JAXBException, XMLStreamException {
    try {
      parse(inputStream, LocalDate.now(), fileSource);
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  @Audited(event = "BLOOMBERG_PARSE_EMAIL_XML_MULTIPART")
  public void processXml(MultipartFile multipartFile, LocalDate importDate)
      throws IOException, JAXBException, XMLStreamException {
    try {
      parse(multipartFile.getInputStream(), importDate, multipartFile.getName());
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  private CommunicationChainDto parseMessage(
      @NonNull final BloombergEmailMessage message,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource) {

    final var chainLinkId = message.getMsgID();

    final var timeSent =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(message.getMsgTimeUTC()), ZoneId.systemDefault());

    final var sender = message.getSender().getUserInfo().getBloombergEmailAddress();

    final var content =
        CommunicationMessageDto.builder()
            .chainLinkId(chainLinkId)
            .sourceMessageId(message.getMsgID())
            .subject(message.getSubject())
            .message(message.getMsgBody())
            .sender(sender)
            .timeSent(timeSent)
            .source(SourceType.BLOOMBERG_MAIL.toString())
            .importDate(importDate)
            .fileSource(fileSource)
            .build();

    final var messageAttachments = message.getAttachment();
    final var originalSender = message.getOriginalSender();
    final var sharedMessenger = message.getSharedMessenger();

    final var hasMetadata =
        !ListUtils.isNullOrEmpty(messageAttachments)
            || originalSender != null
            || sharedMessenger != null;

    if (hasMetadata) {

      final var metadata = BloombergEmailMetadata.builder().build();

      if (originalSender != null && originalSender.getUserInfo() != null) {

        final var user = originalSender.getUserInfo();
        metadata.setOriginalSender(
            BloombergEmailOriginalSender.builder().sender(user.getBloombergEmailAddress()).build());
      }

      if (sharedMessenger != null && sharedMessenger.getUserInfo() != null) {

        final var user = sharedMessenger.getUserInfo();
        metadata.setSharedMessenger(
            BloombergEmailSharedMessenger.builder()
                .sender(user.getBloombergEmailAddress())
                .build());
      }

      final var attachments =
          messageAttachments.stream()
              .filter(attachment -> !Strings.isEmpty(attachment.getFileName()))
              .map(attachment -> BloombergAttachment.builder().build())
              .collect(Collectors.toList());

      if (!attachments.isEmpty()) {
        metadata.setAttachments(attachments);
      }

      final var containsMetadata =
          !attachments.isEmpty() || originalSender != null || sharedMessenger != null;

      if (containsMetadata) {
        content.setType(MessageType.BLOOMBERG_EMAIL.toString());
        content.setMetadata(metadata);
      }
    }

    final var subject = message.getSubject();

    final var recipients = parseRecipients(message, importDate, fileSource);

    final var users = parseUsers(message, fileSource);

    eventPublisher.addToExternalUserBatch(users);
    eventPublisher.addToMessageBatch(List.of(content));
    eventPublisher.addToRecipientBatch(recipients);

    return CommunicationChainDto.builder()
        .source(SourceType.BLOOMBERG_MAIL.toString())
        .sourceChainId(chainLinkId)
        .subject(subject)
        .dateFrom(timeSent)
        .dateImported(importDate)
        .datePeriod(timeSent.toLocalDate())
        .fileSource(fileSource)
        .build();
  }

  private List<CommunicationRecipientDto> parseRecipients(
      @NonNull final BloombergEmailMessage message,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource) {

    final var chainLinkId = message.getMsgID();

    return message.getRecipient().stream()
        .map(recipient -> mapRecipient(recipient, chainLinkId, importDate, fileSource))
        .collect(Collectors.toList());
  }

  private CommunicationRecipientDto mapRecipient(
      @NonNull final BloombergEmailRecipient emailRecipient,
      @NonNull final String chainLinkId,
      @NonNull final LocalDate importDate,
      @NonNull final String fileSource) {

    final var recipient =
        CommunicationRecipientDto.builder()
            .chainLinkId(chainLinkId)
            .dateImported(importDate)
            .fileSource(fileSource)
            .build();

    final var userInfo = emailRecipient.getUserInfo();

    if (userInfo != null) {

      recipient.setRecipient(userInfo.getBloombergEmailAddress());

      Optional.ofNullable(emailRecipient.getForwardedBy())
          .ifPresent(
              forwardedBy ->
                  recipient.setMetadata(
                      BloombergEmailRecipientMetadata.builder().forwardedBy(forwardedBy).build()));

      final var forwardTo = emailRecipient.getForwardedTo();

      if (forwardTo == null) {
        return recipient;
      }

      final var forwardToRecipient = forwardTo.getRecipient();

      if (forwardToRecipient == null) {
        return recipient;
      }

      final var forwardToUserInfo = forwardToRecipient.getUserInfo();

      if (forwardToUserInfo != null) {
        recipient.setMetadata(
            BloombergEmailRecipientMetadata.builder()
                .forwardedTo(forwardToUserInfo.getBloombergEmailAddress())
                .build());
      }
    }

    return recipient;
  }

  private List<ExternalUserDto> parseUsers(
      @NonNull final BloombergEmailMessage message, @NonNull final String fileSource) {

    final var users = new ArrayList<ExternalUserDto>();

    if (message.getSender() != null) {

      Optional.ofNullable(message.getSender().getUserInfo())
          .ifPresent(info -> users.add(mapToExternalUser(info, fileSource)));
    }

    users.addAll(
        message.getRecipient().stream()
            .map(recipient -> mapRecipientUsers(recipient, fileSource))
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));

    return users;
  }

  private List<ExternalUserDto> mapRecipientUsers(
      @NonNull final BloombergEmailRecipient recipient, @NonNull final String fileSource) {

    final var users = new ArrayList<ExternalUserDto>();

    Optional.ofNullable(recipient.getUserInfo())
        .ifPresent(info -> users.add(mapToExternalUser(info, fileSource)));

    final var forwardTo = recipient.getForwardedTo();

    if (forwardTo != null && forwardTo.getRecipient() != null) {

      Optional.ofNullable(forwardTo.getRecipient().getUserInfo())
          .ifPresent(info -> users.add(mapToExternalUser(info, fileSource)));
    }

    return users;
  }

  private ExternalUserDto mapToExternalUser(
      @NonNull final BloombergEmailUserInfo userInfo, @NonNull final String fileSource) {
    return ExternalUserDto.builder()
        .source(Source.BLOOMBERG.toString())
        .externalId(userInfo.getBloombergEmailAddress())
        .email(userInfo.getBloombergEmailAddress())
        .firstName(userInfo.getFirstName())
        .lastName(userInfo.getLastName())
        .fileSource(fileSource)
        .build();
  }
}
