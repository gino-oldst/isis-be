package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.NotificationLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.NotificationLog;
import st.old.isis.storage.repositories.NotificationLogRepository;
import st.old.isis.storage.specifications.NotificationLogSpecification;
import st.old.isis.storage.validators.NotificationLogValidator;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationLogService {

    private final NotificationLogRepository repository;
    private final NotificationLogValidator validator;

    @Audited(event = "CREATE_NOTIFICATION_LOG")
    public NotificationLogDto create(@NonNull @Audited final NotificationLogDto dto) throws IsisException {

        validator.validate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    @Audited(event = "UPDATE_NOTIFICATION_LOG")
    public NotificationLogDto update(@NonNull @Audited final NotificationLogDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        validator.updateValidate(oldEntity, dto);

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    @Audited(event = "GET_NOTIFICATION_LOG_BY_ID")
    public NotificationLogDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_NOTIFICATION_LOG")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_NOTIFICATION_LOG")
    public ListDto<NotificationLogDto> search(@NonNull @Audited final NotificationLogDto filter) {

        final var spec = new NotificationLogSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<NotificationLogDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_NOTIFICATION_LOG")
    public ListDto<NotificationLogDto> search(@NonNull @Audited final NotificationLogDto filter, @Audited Pageable pageable) {

        final var spec = new NotificationLogSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<NotificationLogDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private NotificationLog findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find notification log with id: [%s]", id),
                        "NotFoundException"));
    }

    private NotificationLog mapToEntity(@NonNull final NotificationLogDto dto) {
        return NotificationLog.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .message(dto.getMessage())
                .alertId(dto.getAlertId())
                .alertStatus(dto.getAlertStatus())
                .seen(dto.isSeen())
                .dateCreated(dto.getDateCreated())
                .deleted(dto.isDeleted())
                .build();
    }

    private NotificationLogDto mapToResource(@NonNull final NotificationLog entity) {
        return NotificationLogDto.builder()
                .id(entity.getId())
                .id(entity.getId())
                .userId(entity.getUserId())
                .message(entity.getMessage())
                .alertId(entity.getAlertId())
                .alertStatus(entity.getAlertStatus())
                .seen(entity.isSeen())
                .dateCreated(entity.getDateCreated())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private NotificationLog mapToEntity(@NonNull NotificationLog oldEntity,
                                        @NonNull NotificationLogDto newDto) {

        // final var entity = mapToEntity(newDto);
        // entity.setDateCreated(oldEntity.getDateCreated());
        //
        // return entity;
        final var updatedEntity= mapToResource(oldEntity);
        updatedEntity.setDateCreated(oldEntity.getDateCreated());

        if(newDto.isSeen()){
            updatedEntity.setSeen(newDto.isSeen());
        }

        return mapToEntity(updatedEntity);

    }
}
