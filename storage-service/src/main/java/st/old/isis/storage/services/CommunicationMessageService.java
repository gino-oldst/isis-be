package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.repositories.CommunicationMessageRepository;
import st.old.isis.storage.specifications.CommunicationMessageSpecification;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationMessageService {

    private final CommunicationMessageRepository repository;

    public CommunicationMessageDto create(@NonNull final CommunicationMessageDto dto) {

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public CommunicationMessageDto update(@NonNull final CommunicationMessageDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        final var newEntity = mapToEntity(dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    public CommunicationMessageDto getById(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public void delete(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<CommunicationMessageDto> search(@NonNull final CommunicationMessageDto filter) {

        final var spec = new CommunicationMessageSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<CommunicationMessageDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    public ListDto<CommunicationMessageDto> search(@NonNull final CommunicationMessageDto filter, Pageable pageable) {

        final var spec = new CommunicationMessageSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<CommunicationMessageDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private CommunicationMessage findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find a message with id: [%s]", id),
                        "NotFoundException"));
    }

    private CommunicationMessage mapToEntity(@NonNull final CommunicationMessageDto dto) {
        return CommunicationMessage.builder()
                .id(dto.getId())
                .chainLinkId(dto.getChainLinkId())
                .message(dto.getMessage())
                .sourceMessageId(dto.getSourceMessageId())
                .timeSent(dto.getTimeSent())
                .importDate(dto.getImportDate())
                .sender(dto.getSender())
                .build();
    }

    private CommunicationMessageDto mapToResource(@NonNull final CommunicationMessage entity) {
        return CommunicationMessageDto.builder()
                .id(entity.getId())
                .chainLinkId(entity.getChainLinkId())
                .message(entity.getMessage())
                .sourceMessageId(entity.getSourceMessageId())
                .timeSent(entity.getTimeSent())
                .importDate(entity.getImportDate())
                .sender(entity.getSender())
                .build();
    }
}
