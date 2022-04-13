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
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.repositories.CommunicationMessageRepository;
import st.old.isis.storage.specifications.CommunicationMessageSpecification;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickCommunicationService {

    private final CommunicationMessageRepository communicationMessageRepository;

    private final CommunicationMessageService communicationMessageService;

    private final AlertLogService alertLogService;

    private final CommunicationRecipientService communicationRecipientService;

    @Audited(event = "QUICK_SEARCH_COMMUNICATION_MESSAGE")
    public ListDto<QuickCommunicationDto> search(@NonNull @Audited final CommunicationMessageDto filter, Pageable pageable) throws IsisException {

        final var spec = new CommunicationMessageSpecification(filter);

        // Retrieve pageable communication messages
        final var searchResults = communicationMessageRepository.findAll(spec, pageable);

        if(searchResults.isEmpty()){
            throw IsisException.notFound("No communication message found with filter indicated", "NotFoundException");
        }

        final var quickCommunications= new ArrayList<>();

        final var quickComms= searchResults.stream().map(message -> {

            // For each message, retrieve corresponding alert logs
            final var alertFilter = AlertLogDto.builder()
                    .commMessageId(message.getId())
                    .build();

            final var alerts= alertLogService.search(alertFilter);

            // For each message, get corresponding recipients

            final var recpFilter= CommunicationRecipientDto.builder()
                    .chainLinkId(message.getChainLinkId())
                    .build();

            final var recipients= communicationRecipientService.search(recpFilter);

            return QuickCommunicationDto.builder()
                    .message(communicationMessageService.mapToResource(message))
                    .alerts(alerts)
                    .recipients(recipients)
                    .build();
        }).collect(Collectors.toList());


        return ListDto.<QuickCommunicationDto>builder()
                .data(quickComms)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();

    }


}
