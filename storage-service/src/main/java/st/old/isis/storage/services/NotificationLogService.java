package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
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

    public NotificationLogDto create(@NonNull final NotificationLogDto dto) throws IsisException {

        validator.validate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public NotificationLogDto update(@NonNull final NotificationLogDto dto) throws IsisException {

        validator.validate(dto);

        final var newEntity = mapToEntity(dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    public NotificationLogDto getById(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public void delete(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<NotificationLogDto> search(@NonNull final NotificationLogDto filter) {

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

    public ListDto<NotificationLogDto> search(@NonNull final NotificationLogDto filter, Pageable pageable) {

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
                .seen(entity.isSeen())
                .dateCreated(entity.getDateCreated())
                .deleted(entity.isDeleted())
                .build();
    }
}
