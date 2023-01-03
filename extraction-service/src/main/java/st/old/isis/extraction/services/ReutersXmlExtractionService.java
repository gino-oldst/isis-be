package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.annotations.Audited;
import st.old.isis.constants.ReutersConstants;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.entities.generated.ComplianceActivity;
import st.old.isis.enums.Source;
import st.old.isis.enums.SourceType;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.context.DownloadContext;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.services.internal.StorageService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReutersXmlExtractionService {

  private final StorageService storageService;
  private final NotificationService notificationService;
  private final OpenSearchService openSearchService;

  @Audited(event = "PARSE_XML_STREAM")
  public ListDto<CommunicationChainDto> processXml(
      @NonNull final InputStream inputStream, @NonNull final String fileSource, LocalDate localDate)
      throws JAXBException, IsisException {
    try {
      return processXmlViaOpenSearch(new InputStreamReader(inputStream), localDate, fileSource);
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  @Audited(event = "PARSE_XML_MULTIPART")
  public ListDto<CommunicationChainDto> processXml(
      MultipartFile multipartFile, LocalDate importDate)
      throws IOException, JAXBException, IsisException {
    try {
      return processXmlViaOpenSearch(
          new InputStreamReader(multipartFile.getInputStream()),
          importDate,
          multipartFile.getName());
    } catch (Throwable err) {
      sendParsingError(err);
      throw err;
    }
  }

  public ListDto<CommunicationChainDto> processXml(
      Reader reader, LocalDate importDate, String fileSource) throws JAXBException, IsisException {

    log.info("START - Processing Reuters XML File for date {}", importDate);

    // TODO In the future, possible to change to annotation based configuration
    JAXBContext context = JAXBContext.newInstance(ComplianceActivity.class);
    final var activity =
        (ComplianceActivity) context.createUnmarshaller().unmarshal(new BufferedReader(reader));

    // Process userLink entries
    final var communicationRecipients = parseCommunicationRecipients(activity, fileSource);
    storageService.saveExternalUsers(communicationRecipients);

    // Process communication logs
    final var communicationChains = parseCommunicationChains(activity, importDate, fileSource);

    // Calls storage service to persist chain into the database
    final var importedChains = storageService.importChain(communicationChains);
    log.info("Imported {} communication chains complete", importedChains.getTotalNoOfRecords());

    log.info("COMPLETE - Processing Reuters XML File for date {}", importDate);

    DownloadContext.addCompletedFile(fileSource);

    return communicationChains;
  }

  public ListDto<CommunicationChainDto> processXmlViaOpenSearch(
          Reader reader, LocalDate importDate, String fileSource) throws JAXBException, IsisException {

    log.info("START - Processing Reuters XML File for date {}", importDate);

    // TODO In the future, possible to change to annotation based configuration
    JAXBContext context = JAXBContext.newInstance(ComplianceActivity.class);
    final var activity =
            (ComplianceActivity) context.createUnmarshaller().unmarshal(new BufferedReader(reader));

    // Process userLink entries
//    final var communicationRecipients = parseCommunicationRecipients(activity, fileSource);
//    storageService.saveExternalUsers(communicationRecipients);

    // Process communication logs
    final var communicationChains = ingestCommunicationData(activity, importDate, fileSource);

    log.info("COMPLETE - Processing Reuters XML File for date {}", importDate);

    DownloadContext.addCompletedFile(fileSource);

    return communicationChains;
  }

  private ListDto<CommunicationChainDto> parseCommunicationChains(
      ComplianceActivity activity, LocalDate importDate, String fileSource) throws IsisException {

    log.info("START - Parsing Communication Chains");

    final var datePeriod = parseDatePeriodFromDateFrom(activity.getHeaders());
    log.info("Parsed date period from header date from {}", datePeriod);

    if (null != activity.getChats() && !activity.getChats().getChat().isEmpty()) {

      final var chatsList = activity.getChats().getChat();

      final List<CommunicationChainDto> commLogList = new ArrayList<>();

      for (ComplianceActivity.Chats.Chat chat : chatsList) {

        final List<CommunicationMessageDto> commMessageList = new ArrayList<>();

        final var sourceChainId = chat.getIdentifier();
        final var recipients =
            parseChatParticipants(chat.getParticipants(), sourceChainId, importDate, fileSource);

        if (null != chat.getEvents() && !chat.getEvents().getEvent().isEmpty()) {

          final var eventList = chat.getEvents().getEvent();

          for (ComplianceActivity.Chats.Chat.Events.Event event : eventList) {

            final var eventUser = event.getUser();

            if (Strings.isEmpty(eventUser)) {
              continue;
            }

            // TODO Cleanup validation here

            if (event.getType().equals(ReutersConstants.EVENT_TYPE_SEND)) {
              final var message = event.getMessage().get(0);
              final var timeSent =
                  event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

              final var commMessage =
                  CommunicationMessageDto.builder()
                      .chainLinkId(sourceChainId)
                      .message(message.getContent())
                      .sourceMessageId(String.valueOf(message.getMessageID()))
                      .timeSent(timeSent)
                      .sender(eventUser)
                      .importDate(importDate)
                      .type(ReutersConstants.EVENT_TYPE_SEND)
                      .source(SourceType.REUTERS.toString())
                      .fileSource(fileSource)
                      .build();

              commMessageList.add(commMessage);
            }
            else if (event.getType().equals(ReutersConstants.EVENT_TYPE_JOIN)) {
              final var message = String.format("*** %s has joined the room", event.getUser());
              final var timeSent =
                      event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

              final var commMessage =
                  CommunicationMessageDto.builder()
                      .chainLinkId(sourceChainId)
                      .message(message)
                      // .sourceMessageId(String.valueOf(message.getMessageID()))
                      .timeSent(timeSent)
                      .sender(eventUser)
                      .importDate(importDate)
                      .type(ReutersConstants.EVENT_TYPE_JOIN)
                      .source(SourceType.REUTERS.toString())
                      .fileSource(fileSource)
                      .build();

              commMessageList.add(commMessage);
            }
            else if (event.getType().equals(ReutersConstants.EVENT_TYPE_LEAVE)) {
              final var message = String.format("*** %s has left the room", event.getUser());
              final var timeSent =
                      event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

              final var commMessage =
                      CommunicationMessageDto.builder()
                              .chainLinkId(sourceChainId)
                              .message(message)
                              // .sourceMessageId(String.valueOf(message.getMessageID()))
                              .timeSent(timeSent)
                              .sender(eventUser)
                              .importDate(importDate)
                              .type(ReutersConstants.EVENT_TYPE_LEAVE)
                              .source(SourceType.REUTERS.toString())
                              .fileSource(fileSource)
                              .build();

              commMessageList.add(commMessage);
            }
          }

//          storageService.saveMessages(commMessageList);
//          storageService.saveRecipients(recipients);

          final var commLog =
              CommunicationChainDto.builder()
                  .source(SourceType.REUTERS.toString())
                  .sourceChainId(sourceChainId)
                  .datePeriod(datePeriod)
                  .dateImported(importDate)
                  .fileSource(fileSource)
                  .build();

          commLogList.add(commLog);
        }
      }

      log.info("COMPLETE - Parsing Communication Chains");

      return ListDto.<CommunicationChainDto>builder()
          .data(commLogList)
          .totalNoOfRecords(commLogList.size())
          .totalCurrentElements(commLogList.size())
          .build()
          .success();
    }

    return ListDto.emptyList();
  }

  private ListDto<CommunicationChainDto> ingestCommunicationData(
          ComplianceActivity activity, LocalDate importDate, String fileSource) throws IsisException {

    log.info("START - Ingest Communication Data for OpenSearch");

    final var datePeriod = parseDatePeriodFromDateFrom(activity.getHeaders());
    log.info("Parsed date period from header date from {}", datePeriod);

    if (null != activity.getChats() && !activity.getChats().getChat().isEmpty()) {

      final var chatsList = activity.getChats().getChat();

      final List<CommunicationChainDto> commLogList = new ArrayList<>();

      for (ComplianceActivity.Chats.Chat chat : chatsList) {

        final List<CommunicationMessageDto> commMessageList = new ArrayList<>();

        final var sourceChainId = chat.getIdentifier();
        final var recipients =
                parseChatParticipants(chat.getParticipants(), sourceChainId, importDate, fileSource);

        final var strRecipients = recipients.stream().map(CommunicationRecipientDto::getRecipient).collect(Collectors.toList());

        if (null != chat.getEvents() && !chat.getEvents().getEvent().isEmpty()) {

          final var eventList = chat.getEvents().getEvent();

          for (ComplianceActivity.Chats.Chat.Events.Event event : eventList) {

            final var eventUser = event.getUser();

            if (Strings.isEmpty(eventUser)) {
              continue;
            }

            // TODO Cleanup validation here

            if (event.getType().equals(ReutersConstants.EVENT_TYPE_SEND)) {
              final var message = event.getMessage().get(0);
              final var timeSent =
                      event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

              final var commMessage =
                      CommunicationMessageDto.builder()
                              .chainLinkId(sourceChainId)
                              .message(message.getContent())
                              .sourceMessageId(String.valueOf(message.getMessageID()))
                              .timeSent(timeSent)
                              .sender(eventUser)
                              .importDate(importDate)
                              .type(ReutersConstants.EVENT_TYPE_SEND)
                              .source(SourceType.REUTERS.toString())
                              .fileSource(fileSource)
                              .recipients(strRecipients)
                              .build();

              commMessageList.add(commMessage);
            }
            else if (event.getType().equals(ReutersConstants.EVENT_TYPE_JOIN)) {
              final var message = String.format("*** %s has joined the room", event.getUser());
              final var timeSent =
                      event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

              final var commMessage =
                      CommunicationMessageDto.builder()
                              .chainLinkId(sourceChainId)
                              .message(message)
                              // .sourceMessageId(String.valueOf(message.getMessageID()))
                              .timeSent(timeSent)
                              .sender(eventUser)
                              .importDate(importDate)
                              .type(ReutersConstants.EVENT_TYPE_JOIN)
                              .source(SourceType.REUTERS.toString())
                              .fileSource(fileSource)
                              .recipients(strRecipients)
                              .build();

              commMessageList.add(commMessage);
            }
            else if (event.getType().equals(ReutersConstants.EVENT_TYPE_LEAVE)) {
              final var message = String.format("*** %s has left the room", event.getUser());
              final var timeSent =
                      event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

              final var commMessage =
                      CommunicationMessageDto.builder()
                              .chainLinkId(sourceChainId)
                              .message(message)
                              // .sourceMessageId(String.valueOf(message.getMessageID()))
                              .timeSent(timeSent)
                              .sender(eventUser)
                              .importDate(importDate)
                              .type(ReutersConstants.EVENT_TYPE_LEAVE)
                              .source(SourceType.REUTERS.toString())
                              .fileSource(fileSource)
                              .recipients(strRecipients)
                              .build();

              commMessageList.add(commMessage);
            }
          }

//          storageService.saveMessages(commMessageList);
//          storageService.saveRecipients(recipients);

          final var commLog =
                  CommunicationChainDto.builder()
                          .source(SourceType.REUTERS.toString())
                          .sourceChainId(sourceChainId)
                          .datePeriod(datePeriod)
                          .dateImported(importDate)
                          .fileSource(fileSource)
                          .build();

          commLogList.add(commLog);
        }

        try {
          openSearchService.indexBatch(commMessageList);
        } catch (IOException ioe) {
          log.error("An error occurred during batch index for OpenSearch", ioe);
          throw IsisException.exception("An error occurred during batch index for OpenSearch", ioe);
        }

      }

      log.info("COMPLETE - Parsing Communication Chains");

      return ListDto.<CommunicationChainDto>builder()
              .data(commLogList)
              .totalNoOfRecords(commLogList.size())
              .totalCurrentElements(commLogList.size())
              .build()
              .success();
    }

    return ListDto.emptyList();
  }

  private LocalDate parseDatePeriodFromDateFrom(ComplianceActivity.Headers headers) {
    final var dateFrom = headers.getDataPeriod().getFrom();
    return dateFrom.toGregorianCalendar().toZonedDateTime().toLocalDate();
  }

  private List<CommunicationRecipientDto> parseChatParticipants(
      ComplianceActivity.Chats.Chat.Participants participants,
      String sourceChainId,
      LocalDate importDate,
      String fileSource) {

    if (null != participants.getUser() && !participants.getUser().isEmpty()) {

      final var users = participants.getUser();

      return users.stream()
          .filter(user -> !Strings.isEmpty(user))
          .map(
              user ->
                  CommunicationRecipientDto.builder()
                      .chainLinkId(sourceChainId)
                      .recipient(user)
                      .dateImported(importDate)
                      .fileSource(fileSource)
                      .build())
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  private List<ExternalUserDto> parseCommunicationRecipients(
      @NonNull ComplianceActivity activity, @NonNull String fileSource) {

    if (null != activity.getUsers() && !activity.getUsers().getUserInfo().isEmpty()) {

      final var userInfoList = activity.getUsers().getUserInfo();

      return new ArrayList<>(
          userInfoList.stream()
              .map(user -> mapToUserDto(user, fileSource))
              .collect(
                  Collectors.toMap(
                      ExternalUserDto::getExternalId, Function.identity(), (v1, v2) -> v1))
              .values());
    }

    return new ArrayList<>();
  }

  public ExternalUserDto mapToUserDto(
      ComplianceActivity.Users.UserInfo userInfo, String fileSource) {

    return ExternalUserDto.builder()
        .source(Source.REUTERS.toString())
        .externalId(userInfo.getIdentifier())
        .email(userInfo.getEmail())
        .firstName(userInfo.getFirstName())
        .lastName(userInfo.getLastName())
        .fileSource(fileSource)
        .build();
  }

  private void sendParsingError(Throwable e) {
    log.error("An error occurred when parsing Reuters raw file", e);

    final var request =
        NotificationRequestDto.builder()
            .customMessage(
                MessageDto.builder()
                    .subject("Download Failed")
                    .body(
                        String.format(
                            "Error parsing Reuters raw data file failed: %s", e.getMessage()))
                    .build())
            // .recipients() todo: recipients
            .build();

    //    notificationService.sendNotification(List.of(request));
  }
}
