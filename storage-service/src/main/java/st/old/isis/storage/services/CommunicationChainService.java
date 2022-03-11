package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.enums.SourceType;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationChain;
import st.old.isis.storage.repositories.CommunicationChainRepository;
import st.old.isis.storage.specifications.CommunicationChainSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationChainService {

    private final CommunicationChainRepository repository;

    private final CommunicationMessageService messageService;

    private final CommunicationRecipientService recipientService;

    private final DataImportRecordService dataImportRecordService;

    @Audited(event = "IMPORT_COMMUNICATION_CHAIN_DATA")
    public ListDto<CommunicationChainDto> importData(ListDto<CommunicationChainDto> request) throws IsisException {

        List<CommunicationChainDto> results = new ArrayList<>();

        request.getData().stream().forEach(chain -> {

            final var newChain= repository.save(mapToEntity(chain));
            final var chainLinkId= newChain.getId();
            final var importDate= chain.getDateImported();

            final var messages= chain.getMessages().stream()
                    .map(message -> {
                        message.setChainLinkId(chainLinkId);
                        message.setImportDate(importDate);
                        return messageService.create(message);
                    })
                    .collect(Collectors.toList());

            final var recipients = chain.getRecipients().stream()
                    .map(recipient -> {
                        recipient.setChainLinkId(chainLinkId);
                        recipient.setDateImported(importDate);
                        return recipientService.create(recipient);
                    })
                    .collect(Collectors.toList());

            final var chainDto= mapToResource(newChain);
            chainDto.setMessages(messages);
            chainDto.setRecipients(recipients);

            results.add(chainDto);
        });

        //TODO When factory pattern is implemented,
        // we can include post factory call instead of calling from inside individual services
        // ex. Import Reuters then Bloomberg then ... then create data import record

        DataImportRecordDto dataImportRecordDto= DataImportRecordDto.builder()
                .importDate(results.get(0).getDateImported())
                .processedStatus(ProcessedStatus.PENDING_PROCESS)
                .source(SourceType.REUTERS.toString())  //TODO Remove this once factory implementation is done.
                .build();

        dataImportRecordService.create(dataImportRecordDto);

        return ListDto.<CommunicationChainDto>builder()
                .data(results)
                .totalNoOfRecords(results.size())
                .build().success();
    }

    @Audited(event = "GET_COMMUNICATION_CHAIN_BY_ID")
    public CommunicationChainDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_COMMUNICATION_CHAIN")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_COMMUNICATION_CHAIN")
    public ListDto<CommunicationChainDto> search(@NonNull @Audited final CommunicationChainDto filter) {

        final var spec = new CommunicationChainSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<CommunicationChainDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_COMMUNICATION_CHAIN")
    public ListDto<CommunicationChainDto> search(@NonNull @Audited final CommunicationChainDto filter,@Audited Pageable pageable) {

        final var spec = new CommunicationChainSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<CommunicationChainDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private CommunicationChain findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find communication chain with id: [%s]", id),
                        "NotFoundException"));
    }

    private CommunicationChain mapToEntity(@NonNull final CommunicationChainDto dto) {
        return CommunicationChain.builder()
                .id(dto.getId())
                .source(dto.getSource())
                .sourceChainId(dto.getSourceChainId())
                .subject(dto.getSubject())
                .datePeriod(dto.getDatePeriod())
                .dateImported(dto.getDateImported())
                .dateFrom(dto.getDateFrom())
                .dateTo(dto.getDateTo())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private CommunicationChainDto mapToResource(@NonNull final CommunicationChain entity) {
        return CommunicationChainDto.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .sourceChainId(entity.getSourceChainId())
                .subject(entity.getSubject())
                .datePeriod(entity.getDatePeriod())
                .dateImported(entity.getDateImported())
                .dateFrom(entity.getDateFrom())
                .dateTo(entity.getDateTo())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    private CommunicationChainDto mapToResource(@NonNull final CommunicationChain entity,
                                                @NonNull final List<CommunicationRecipientDto> recipients) {
        return CommunicationChainDto.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .sourceChainId(entity.getSourceChainId())
                .subject(entity.getSubject())
                .datePeriod(entity.getDatePeriod())
                .dateImported(entity.getDateImported())
                .dateFrom(entity.getDateFrom())
                .dateTo(entity.getDateTo())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private CommunicationChain mapToEntity(@NonNull CommunicationChain oldEntity,
                                           @NonNull CommunicationChainDto newDto) {

        final var entity = mapToEntity(newDto);
        entity.setDateCreated(oldEntity.getDateCreated());

        return entity;
    }

}
