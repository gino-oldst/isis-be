package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.annotations.Audited;
import st.old.isis.constants.ReutersConstants;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.UserDto;
import st.old.isis.entities.generated.ComplianceActivity;
import st.old.isis.enums.SourceType;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.internal.StorageService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReutersXmlExtractionService {

    private final StorageService storageService;

    @Audited(event = "PARSE_XML_STREAM")
    public ListDto<CommunicationChainDto> processXml(
            @NonNull final InputStream inputStream) throws JAXBException, IsisException {
        return processXml(new InputStreamReader(inputStream), LocalDate.now());
    }

    @Audited(event = "PARSE_XML_MULTIPART")
    public ListDto<CommunicationChainDto> processXml(
            MultipartFile multipartFile, LocalDate importDate) throws IOException, JAXBException, IsisException {
        return processXml(new InputStreamReader(multipartFile.getInputStream()), importDate);
    }

    public ListDto<CommunicationChainDto> processXml(Reader reader, LocalDate importDate)
            throws JAXBException, IsisException {

        log.info("START - Processing Reuters XML File for date {}", importDate);

        //TODO In the future, possible to change to annotation based configuration
        JAXBContext context = JAXBContext.newInstance(ComplianceActivity.class);
        final var activity = (ComplianceActivity) context.createUnmarshaller()
                .unmarshal(new BufferedReader(reader));

        //Process userLink entries
        final var communicationRecipients = parseCommunicationRecipients(activity);

        //TODO call userLink service here

        //Process communication logs
        final var communicationChains = parseCommunicationChains(activity, importDate);

        //Calls storage service to persist chain into the database
        final var importedChains = storageService.importChain(communicationChains);
        log.info("Imported {} communication chains complete", importedChains.getTotalNoOfRecords());

        log.info("COMPLETE - Processing Reuters XML File for date {}", importDate);

        return communicationChains;

    }

    private ListDto<CommunicationChainDto> parseCommunicationChains(ComplianceActivity activity, LocalDate importDate) {

        log.info("START - Parsing Communication Chains");

        final var datePeriod = parseDatePeriodFromDateFrom(activity.getHeaders());
        log.info("Parsed date period from header date from {}", datePeriod);

        if (null != activity.getChats() && !activity.getChats().getChat().isEmpty()) {

            final var chatsList = activity.getChats().getChat();

            final List<CommunicationChainDto> commLogList = new ArrayList<>();

            for (ComplianceActivity.Chats.Chat chat : chatsList) {

                final List<CommunicationMessageDto> commMessageList = new ArrayList<>();

                final var sourceChainId = chat.getIdentifier();
                final var recipients = parseChatParticipants(chat.getParticipants());

                if (null != chat.getEvents() && !chat.getEvents().getEvent().isEmpty()) {

                    final var eventList = chat.getEvents().getEvent();

                    for (ComplianceActivity.Chats.Chat.Events.Event event : eventList) {

                        final var eventUser = event.getUser();

                        //TODO Cleanup validation here

                        if (event.getType().equals(ReutersConstants.EVENT_TYPE_SEND)) {

                            final var message = event.getMessage().get(0);
                            final var timeSent = event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

                            final var commMessage = CommunicationMessageDto.builder()
                                    .message(message.getContent())
                                    .sourceMessageId(String.valueOf(message.getMessageID()))
                                    .timeSent(timeSent)
                                    .sender(eventUser)
                                    .build();

                            commMessageList.add(commMessage);

                        }

                    }


                    final var commLog = CommunicationChainDto.builder()
                            .source(SourceType.REUTERS.toString())
                            .sourceChainId(sourceChainId)
                            .datePeriod(datePeriod)
                            .dateImported(importDate)
                            .messages(commMessageList)
                            .recipients(recipients)
                            .build();

                    commLogList.add(commLog);

                }

            }

            log.info("COMPLETE - Parsing Communication Chains");

            return ListDto.<CommunicationChainDto>builder()
                    .data(commLogList)
                    .totalNoOfRecords(commLogList.size())
                    .totalCurrentElements(commLogList.size())
                    .build().success();

        }

        return ListDto.emptyList();

    }

    private LocalDate parseDatePeriodFromDateFrom(ComplianceActivity.Headers headers) {
        final var dateFrom = headers.getDataPeriod().getFrom();
        return dateFrom.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    private List<CommunicationRecipientDto> parseChatParticipants(ComplianceActivity.Chats.Chat.Participants participants) {

        if (null != participants.getUser() && !participants.getUser().isEmpty()) {

            final var users = participants.getUser();

            return users.stream()
                    .map(user -> CommunicationRecipientDto.builder()
                            .recipient(user)
                            .build()).collect(Collectors.toList());

        }

        return Collections.emptyList();

    }

    private ListDto<UserDto> parseCommunicationRecipients(@NonNull ComplianceActivity activity) {

        if (null != activity.getUsers() && !activity.getUsers().getUserInfo().isEmpty()) {

            final var userInfoList = activity.getUsers().getUserInfo();

            final var userList = userInfoList.stream().map(this::mapToUserDto).collect(Collectors.toList());

            return ListDto.<UserDto>builder()
                    .data(userList)
                    .totalNoOfRecords(userList.size())
                    .totalCurrentElements(userList.size())
                    .build().success();

        }

        return ListDto.emptyList();

    }

    public UserDto mapToUserDto(ComplianceActivity.Users.UserInfo userInfo) {

        return UserDto.builder()
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .build();

    }


}
