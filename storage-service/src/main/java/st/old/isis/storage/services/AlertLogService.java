package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.repositories.AlertLogRepository;
import st.old.isis.storage.specifications.AlertLogSpecification;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertLogService {

    private final AlertLogRepository repository;

    public AlertLogDto create(@NonNull final AlertLogDto dto) {

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public AlertLogDto update(@NonNull final AlertLogDto dto) {

        final var newEntity = mapToEntity(dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    public AlertLogDto getById(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public void delete(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<AlertLogDto> search(@NonNull final AlertLogDto filter) {

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

    public ListDto<AlertLogDto> search(@NonNull final AlertLogDto filter, Pageable pageable) {

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

    private AlertLog findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find alert log with id: [%s]", id),
                        "NotFoundException"));
    }

    private AlertLog mapToEntity(@NonNull final AlertLogDto dto) {
        return AlertLog.builder()
                .id(dto.getId())
                .commLogId(dto.getCommLogId())
                .behaviorId(dto.getBehaviorId())
                .keyword(dto.getKeyword())
                .message(dto.getMessage())
                .status(dto.getStatus())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private AlertLogDto mapToResource(@NonNull final AlertLog entity) {
        return AlertLogDto.builder()
                .id(entity.getId())
                .commLogId(entity.getCommLogId())
                .behaviorId(entity.getBehaviorId())
                .keyword(entity.getKeyword())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }
}
