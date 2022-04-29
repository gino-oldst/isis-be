package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.communication.QuickCommunicationDto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.repositories.AlertLogRepository;
import st.old.isis.storage.repositories.CommunicationMessageRepository;
import st.old.isis.storage.specifications.AlertLogSpecification;
import st.old.isis.storage.specifications.QuickCommunicationSpecification;
import st.old.isis.utils.ListUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickCommunicationService {

    private final CommunicationMessageRepository communicationMessageRepository;

    private final CommunicationMessageService communicationMessageService;

    private final AlertLogRepository alertLogRepository;

    private final AlertLogService alertLogService;

    private final CommunicationRecipientService communicationRecipientService;

    @Audited(event = "SEARCH_PAGED_QUICK_COMMUNICATION")
    public ListDto<QuickCommunicationDto> search(@NonNull @Audited final QuickCommunicationDto filter, @Audited Pageable pageable) throws IsisException {

        var distinctMessageIds = alertLogRepository.findDistinctMessages();

        if(distinctMessageIds.isEmpty()){
            throw IsisException.notFound("No messages found for indicated filter", "NotFoundException");
        }

        final List<QuickCommunicationDto> quickCommList= new ArrayList<>();

        final var filterDateTimeFrom= filter.getDateFrom();
        final var filterDateTimeTo= filter.getDateTo();

        // Filter messages using message fields

        distinctMessageIds =  distinctMessageIds.stream().filter(m -> {
                    try {
                        final var commMessage = communicationMessageService.getById(m);

                        if((filterDateTimeFrom != null && filterDateTimeTo != null) &&
                                (commMessage.getTimeSent().isBefore(filterDateTimeFrom) || commMessage.getTimeSent().isAfter(filterDateTimeTo))) {
                            return false;
                        }

                        if(!ListUtils.isNullOrEmpty(filter.getSenders()) && !filter.getSenders().contains(commMessage.getSender())) {
                            return false;
                        }

                        if(!ListUtils.isNullOrEmpty(filter.getSources()) && !filter.getSources().contains(commMessage.getSource())) {
                            return false;
                        }

                        return true;

                    } catch (IsisException e) {
                        log.error(e.getMessage());
                        return false;
                    }}).collect(Collectors.toList());


        final var keywordIds = filter.getKeywordIds();

        // TODO Filter for alerts

        distinctMessageIds.forEach(messageId -> {

            try {

                // Retrieve alerts for that specific message
                final var alerts = alertLogService.search(AlertLogDto.builder()
                                .keywordIds(filter.getKeywordIds())
                                .statusTypes(filter.getStatusTypes())
                                .commMessageId(messageId).build());

                if(!ListUtils.isNullOrEmpty(alerts.getData())) {
                    // Retrieve message
                    final var message= communicationMessageService.getById(messageId);

                    // Retrieve the recipients
                    final var recipients = communicationRecipientService.search(CommunicationRecipientDto.builder().chainLinkId(message.getChainLinkId()).build());

                    final var quickComm = QuickCommunicationDto.builder()
                            .message(message)
                            .alerts(alerts)
                            .recipients(recipients)
                            .build();

                    quickCommList.add(quickComm);
                }

            } catch (IsisException e) {
                log.error("An error occurred during generation of quick communication object: {}", e.getMessage());
            }

        });

        int start= pageable.getPageNumber() * pageable.getPageSize();
        int end= (pageable.getPageSize() * (pageable.getPageNumber() + 1)) > quickCommList.size()? quickCommList.size() : pageable.getPageSize() * (pageable.getPageNumber() + 1);

        final var totalSize = quickCommList.size();
        final var pageSize = pageable.getPageSize();
        var totalPages = totalSize / pageSize + ((totalSize % pageSize > 0) ? 1 : 0);

        if (start > end) {
            return ListDto.<QuickCommunicationDto>builder()
                    .totalNoOfRecords(totalSize)
                    .totalCurrentElements(0)
                    .totalPages(totalPages)
                    .data(new ArrayList<>())
                    .build()
                    .success();
        }

        final var finalQuickComm = quickCommList.subList(start, end);

        return ListDto.<QuickCommunicationDto>builder()
                .data(finalQuickComm)
                .totalPages(totalPages)
                .totalNoOfRecords(totalSize)
                .totalCurrentElements(finalQuickComm.size())
                .build().success();

    }


}
