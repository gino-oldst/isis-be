package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.SearchCommunicationDto;
import st.old.isis.utils.ListUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCommunicationService {

    private final CommunicationChainService chainService;
    private final CommunicationMessageService messageService;
    private final CommunicationRecipientService recipientService;

    public ListDto<CommunicationMessageDto> search(SearchCommunicationDto filter) {

        Set<String> uniqueRecChainLinkList= new HashSet<>();
        Set<String> uniqueChainLinkList= new HashSet<>();

        // Get unique chains filtered by recipients
        if(!ListUtils.isNullOrEmpty(filter.getRecipients())){

            final var recFilter= CommunicationRecipientDto.builder()
                    .recipients(filter.getRecipients())
                    .dateFrom(filter.getDateFrom())
                    .dateTo(filter.getDateTo())
                    .build();

            final var recListDto = recipientService.search(recFilter);

            if(recListDto != null && !ListUtils.isNullOrEmpty(recListDto.getData())){
                recListDto.getData().stream().forEach(x -> uniqueRecChainLinkList.add(x.getChainLinkId()));
            }

        }

        // Get unique chains by subject
        if(filter.getSubject() != null){

            final var chainFilter = CommunicationChainDto.builder()
                    .subject(filter.getSubject())
                    .filterDateFrom(filter.getDateFrom())
                    .filterDateTo(filter.getDateTo())
                    .build();

            if(!ListUtils.isNullOrEmpty(uniqueRecChainLinkList)){
                chainFilter.setChains(uniqueRecChainLinkList.stream().collect(Collectors.toList()));
            }

            final var chainListDto = chainService.search(chainFilter);

            if(chainListDto != null && !ListUtils.isNullOrEmpty(chainListDto.getData())){
                chainListDto.getData().stream().forEach(x -> uniqueChainLinkList.add(x.getId()));
            }

            //TODO Map Subject and Source

        }

        //Generate message filter

        final var messageFilter = CommunicationMessageDto.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .build();

        if(!ListUtils.isNullOrEmpty(uniqueChainLinkList)){
            messageFilter.setChains(uniqueChainLinkList.stream().collect(Collectors.toList()));
        }

        if(!ListUtils.isNullOrEmpty(filter.getSenders())){
            messageFilter.setSenders(filter.getSenders());
        }

        if(filter.getMessage() != null){
            messageFilter.setMessage(filter.getMessage());
        }

        final var results = messageService.search(messageFilter);

        if(results == null) return ListDto.emptyList();

        return results;

    }


}
