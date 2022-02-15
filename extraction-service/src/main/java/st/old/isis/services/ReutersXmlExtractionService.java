package st.old.isis.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.constants.ReutersConstants;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationLogDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.UserDto;
import st.old.isis.entities.generated.ComplianceActivity;
import st.old.isis.enums.SourceType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReutersXmlExtractionService {

    public void processXml(MultipartFile multipartFile, LocalDate importDate) throws JAXBException, IOException {

        log.info("START - Processing Reuters XML File for date {}", importDate);

        var temp = File.createTempFile(multipartFile.getOriginalFilename(), ".xml");
        temp.deleteOnExit();
        multipartFile.transferTo(temp);

        //TODO In the future, possible to change to annotation based configuration
        JAXBContext context = JAXBContext.newInstance(ComplianceActivity.class);
        final var activity= (ComplianceActivity) context.createUnmarshaller()
                .unmarshal(new FileReader(temp));

        //Process userLink entries
        final var communicationRecipients= parseCommunicationRecipients(activity);

        //TODO Convert to communication activity
        final var communicationLogs = parseCommunicationLogs(activity);



        temp.delete();

        log.info("COMPLETE - Processing Reuters XML File for date {}", importDate);


    }

    private ListDto<CommunicationLogDto> parseCommunicationLogs(ComplianceActivity activity) {

        if(null != activity.getChats() && !activity.getChats().getChat().isEmpty()) {

            final var chatsList = activity.getChats().getChat();

            final List<CommunicationLogDto> communicationLogList = chatsList.stream()
                    .map(chat -> {

                        final var chainLinkId= chat.getIdentifier();
                        final var recipients = parseChatParticipants(chat.getParticipants());

                        if(null != chat.getEvents() && !chat.getEvents().getEvent().isEmpty()) {

                            final var eventList= chat.getEvents().getEvent();

                            for(ComplianceActivity.Chats.Chat.Events.Event event:eventList){

                                //TODO Cleanup validation here

                                if(event.getType().equals(ReutersConstants.EVENT_TYPE_SEND)){

                                    final var message = event.getMessage().get(0);
                                    final var timeSent= event.getUTCTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

                                    return CommunicationLogDto.builder()
                                            .chainLinkId(chainLinkId)
                                            .recipients(recipients)
                                            .sender(event.getUser())
                                            .sourceMessageId(String.valueOf(message.getMessageID()))
                                            .message(message.getContent())
                                            .timeSent(timeSent)
                                            .source(SourceType.REUTERS.toString())
                                            .build();
                                }

                                return null;

                            }
                        }
                        return null;
                    })
                    .filter(out -> out != null)
                    .collect(Collectors.toList());

            return ListDto.<CommunicationLogDto>builder()
                    .data(communicationLogList)
                    .totalNoOfRecords(chatsList.size())
                    .totalCurrentElements(chatsList.size())
                    .build().success();

        }

        return new ListDto<>().emptyList();

    }

    private List<CommunicationRecipientDto> parseChatParticipants(ComplianceActivity.Chats.Chat.Participants participants) {

        if(null != participants.getUser() && !participants.getUser().isEmpty()) {

            final var users= participants.getUser();

            return users.stream()
                    .map(user -> {
                        return CommunicationRecipientDto.builder()
                                .recipient(user)
                                .build();
                    }).collect(Collectors.toList());

        }

        return Collections.emptyList();

    }

    private ListDto<UserDto> parseCommunicationRecipients(@NonNull ComplianceActivity activity) {

        if(null != activity.getUsers() && !activity.getUsers().getUserInfo().isEmpty()) {

            final var userInfoList = activity.getUsers().getUserInfo();

            final var userList = userInfoList.stream().map(this::mapToUserDto).collect(Collectors.toList());

            return ListDto.<UserDto>builder()
                    .data(userList)
                    .totalNoOfRecords(userList.size())
                    .totalCurrentElements(userList.size())
                    .build().success();

        }

        return new ListDto<>().emptyList();

    }

    public UserDto mapToUserDto (ComplianceActivity.Users.UserInfo userInfo) {

        return UserDto.builder()
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .build();

    }


}
