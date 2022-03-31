package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.MessageActivityLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.MessageActivityLog;
import st.old.isis.storage.entities.User;
import st.old.isis.storage.repositories.MessageActivityLogRepository;
import st.old.isis.storage.specifications.MessageActivityLogSpecification;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageActivityLogService {

    private final MessageActivityLogRepository repository;

    @Audited(event = "CREATE_MESSAGE_ACTIVITY_LOG")
    public MessageActivityLogDto create(@NonNull @Audited final MessageActivityLogDto dto) {

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    @Audited(event = "GET_MESSAGE_ACTIVITY_LOG_BY_ID")
    public MessageActivityLogDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_MESSAGE_ACTIVITY_LOG")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_MESSAGE_ACTIVITY_LOG")
    public ListDto<MessageActivityLogDto> search(@NonNull @Audited final MessageActivityLogDto filter) {

        final var spec = new MessageActivityLogSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<MessageActivityLogDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_USER")
    public ListDto<MessageActivityLogDto> search(@NonNull @Audited final MessageActivityLogDto filter, Pageable pageable) {

        final var spec = new MessageActivityLogSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<MessageActivityLogDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private MessageActivityLog findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find user with id: [%s]", id),
                        "NotFoundException"));
    }

    private MessageActivityLog mapToEntity(@NonNull final MessageActivityLogDto dto) {
        return MessageActivityLog.builder()
                .id(dto.getId())
                .messageId(dto.getMessageId())
                .executorId(dto.getExecutorId())
                .event(dto.getEvent())
                .type(dto.getType())
                .dateCreated(dto.getDateCreated())
                .build();
    }

    private MessageActivityLogDto mapToResource(@NonNull final MessageActivityLog entity) {
        return MessageActivityLogDto.builder()
                .id(entity.getId())
                .messageId(entity.getMessageId())
                .executorId(entity.getExecutorId())
                .event(entity.getEvent())
                .type(entity.getType())
                .dateCreated(entity.getDateCreated())
                .build();
    }

    //map read-only properties from old entity to new entity
    private MessageActivityLog mapToEntity(@NonNull User oldEntity, @NonNull MessageActivityLogDto newDto) {

        final var entity = mapToEntity(newDto);
        entity.setDateCreated(oldEntity.getDateCreated());

        return entity;
    }
}
