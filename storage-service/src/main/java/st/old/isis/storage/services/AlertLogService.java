package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.MessageActivityLogDto;
import st.old.isis.enums.ActivityType;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.repositories.AlertLogRepository;
import st.old.isis.storage.specifications.AlertLogSpecification;
import st.old.isis.storage.validators.AlertLogValidator;
import st.old.isis.utils.ListUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertLogService {

    private final AlertLogRepository repository;
    private final AlertLogValidator validator;
    private final MessageActivityLogService messageActivityLogService;

    private final AlertPolicyService alertPolicyService;


    @Audited(event = "CREATE_ALERT_LOG")
    public AlertLogDto create(@NonNull @Audited final AlertLogDto dto) {

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        addActivityLog(createdEntity.getCommMessageId(), "An alert has been generated for this message");

        return mapToResource(createdEntity);
    }

    @Audited(event = "UPDATE_ALERT_LOG")
    public AlertLogDto update(@NonNull @Audited final AlertLogDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        validator.updateValidate(oldEntity, dto);

        final var newEntity = mapToEntity(oldEntity, dto);

        if(newEntity.getNotes() != null) {
            if((oldEntity.getNotes() == null) || !oldEntity.getNotes().equalsIgnoreCase(newEntity.getNotes())){
                addActivityLog(newEntity.getCommMessageId(),
                        String.format(
                                "Note was added for alert [%s] : %s",
                                dto.getPolicyId(), dto.getNotes()));
            }
        }

        if (oldEntity.getStatus() != newEntity.getStatus()) {
            addActivityLog(newEntity.getCommMessageId(),
                    String.format(
                            "Alert [%s] status has been updated to: [%s]",
                            dto.getPolicyId(), dto.getStatus()));
        }

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    @Audited(event = "GET_ALERT_LOG_BY_ID")
    public AlertLogDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_ALERT_LOG")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_ALERT_LOG")
    public ListDto<AlertLogDto> search(@NonNull @Audited final AlertLogDto filter) {

        final var spec = new AlertLogSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<AlertLogDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_PAGED_ALERT_LOG")
    public ListDto<AlertLogDto> search(@NonNull @Audited final AlertLogDto filter, @Audited Pageable pageable) {

        final var spec = new AlertLogSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<AlertLogDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    @Audited(event = "SEARCH_SORT_ALERT_LOG")
    public ListDto<AlertLogDto> search(@NonNull @Audited final AlertLogDto filter, @Audited Sort sort) {

        final var spec = new AlertLogSpecification(filter);

        final var searchResults = repository.findAll(spec, sort);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<AlertLogDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.size())
                .build()
                .success();
    }

    private AlertLog findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find alert log with id: [%s]", id),
                        "NotFoundException"));
    }

    private AlertLog mapToEntity(@NonNull final AlertLogDto dto) {

        return AlertLog.builder()
                .id(dto.getId())
                .commChainLinkId(dto.getCommChainLinkId())
                .commMessageId(dto.getCommMessageId())
                .message(dto.getMessage())
                .policyId(dto.getPolicyId())
                .keyword(dto.getKeyword())
                .keywordId(dto.getKeywordId())
                .description(dto.getDescription())
                .notes(dto.getNotes())
                .status(dto.getStatus())
                .importDate(dto.getImportDate())
                .timeSent(dto.getTimeSent())
                .sender(dto.getSender())
                .recipients(!ListUtils.isNullOrEmpty(dto.getRecipients())? String.join(",",dto.getRecipients()): null)
                .source(dto.getSource())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private AlertLogDto mapToResource(@NonNull final AlertLog entity) {

        AlertPolicyDto policy = null;

        try {
            policy = alertPolicyService.getById(entity.getPolicyId());
        } catch (IsisException e) {
            log.error("An error occurred during retrieval of policy {}", e.getMessage());
        }


        return AlertLogDto.builder()
                .id(entity.getId())
                .commChainLinkId(entity.getCommChainLinkId())
                .commMessageId(entity.getCommMessageId())
                .message(entity.getMessage())
                .policyId(entity.getPolicyId())
                .policyName(policy != null? policy.getName() : null)
                .keyword(entity.getKeyword())
                .keywordId(entity.getKeywordId())
                .description(entity.getDescription())
                .notes(entity.getNotes())
                .status(entity.getStatus())
                .importDate(entity.getImportDate())
                .timeSent(entity.getTimeSent())
                .sender(entity.getSender())
                .source(entity.getSource())
                .recipients(entity.getRecipients() != null? Stream.of(entity.getRecipients().split(",", -1)).collect(Collectors.toList()) : null)
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private AlertLog mapToEntity(@NonNull AlertLog oldEntity, @NonNull AlertLogDto newDto) {

        final var updatedEntity= mapToResource(oldEntity);

        if(newDto.getStatus() != null){
            updatedEntity.setStatus(newDto.getStatus());
        }

        if(newDto.getNotes() != null && !newDto.getNotes().isEmpty()){
            updatedEntity.setNotes(newDto.getNotes());
        }

        return mapToEntity(updatedEntity);
    }

    private void addActivityLog(@NonNull final String messageId, @NonNull final String message) {
        messageActivityLogService.create(
                MessageActivityLogDto.builder()
                        .messageId(messageId)
                        .type(ActivityType.SYSTEM_EVENT)
                        .event(message)
                        .build());
    }
}
