package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.communication.QuickCommunicationDto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.repositories.AlertLogRepository;
import st.old.isis.storage.repositories.CommunicationMessageRepository;
import st.old.isis.storage.specifications.AlertLogSpecification;
import st.old.isis.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

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
    public ListDto<QuickCommunicationDto> search(@NonNull @Audited final AlertLogDto filter, @Audited Pageable pageable) throws IsisException {

        final var spec = new AlertLogSpecification(filter);

        final var messageIds = alertLogRepository.findDistinctMessages(pageable);

        if(messageIds.isEmpty()){
            throw IsisException.notFound("No messages found for indicated filter", "NotFoundException");
        }

        final List<QuickCommunicationDto> quickCommList= new ArrayList<>();

        messageIds.stream().forEach(messageId -> {

            try {

                // Retrieve alerts for that specific message
                final var alerts = alertLogService.search(AlertLogDto.builder().commMessageId(messageId).build());

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

        return ListDto.<QuickCommunicationDto>builder()
                .data(quickCommList)
                .totalPages(messageIds.getTotalPages())
                .totalNoOfRecords(messageIds.getTotalElements())
                .totalCurrentElements(messageIds.getNumberOfElements())
                .build().success();

    }


}
