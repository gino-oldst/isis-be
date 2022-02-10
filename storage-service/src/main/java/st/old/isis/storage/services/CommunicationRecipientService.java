package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationRecipient;
import st.old.isis.storage.repositories.CommunicationRecipientRepository;
import st.old.isis.storage.specifications.CommunicationRecipientSpecification;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationRecipientService {

    private final CommunicationRecipientRepository repository;

    public CommunicationRecipientDto create(@NonNull final CommunicationRecipientDto dto) {

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public CommunicationRecipientDto update(@NonNull final CommunicationRecipientDto dto) {

        final var newEntity = mapToEntity(dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    public CommunicationRecipientDto getById(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public void delete(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<CommunicationRecipientDto> search(@NonNull final CommunicationRecipientDto filter) {

        final var spec = new CommunicationRecipientSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<CommunicationRecipientDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    public ListDto<CommunicationRecipientDto> search(@NonNull final CommunicationRecipientDto filter, Pageable pageable) {

        final var spec = new CommunicationRecipientSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<CommunicationRecipientDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private CommunicationRecipient findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find communication log with id: [%s]", id),
                        "NotFoundException"));
    }

    private CommunicationRecipient mapToEntity(@NonNull final CommunicationRecipientDto dto) {
        return CommunicationRecipient.builder()
                .id(dto.getId())
                .commLogId(dto.getCommLogId())
                .recipient(dto.getRecipient())
                .build();
    }

    private CommunicationRecipientDto mapToResource(@NonNull final CommunicationRecipient entity) {
        return CommunicationRecipientDto.builder()
                .id(entity.getId())
                .commLogId(entity.getCommLogId())
                .recipient(entity.getRecipient())
                .build();
    }
}
