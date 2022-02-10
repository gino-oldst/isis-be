package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationLogDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationLog;
import st.old.isis.storage.repositories.CommunicationLogRepository;
import st.old.isis.storage.specifications.CommunicationLogSpecification;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationLogService {

    private final CommunicationLogRepository repository;

    //Todo: Gino will handle saving communication logs in batch

    public CommunicationLogDto getById(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public void delete(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<CommunicationLogDto> search(@NonNull final CommunicationLogDto filter) {

        final var spec = new CommunicationLogSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<CommunicationLogDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    public ListDto<CommunicationLogDto> search(@NonNull final CommunicationLogDto filter, Pageable pageable) {

        final var spec = new CommunicationLogSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<CommunicationLogDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private CommunicationLog findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find communication log with id: [%s]", id),
                        "NotFoundException"));
    }

    private CommunicationLog mapToEntity(@NonNull final CommunicationLogDto dto) {
        return CommunicationLog.builder()
                .id(dto.getId())
                .chainLinkId(dto.getChainLinkId())
                .sender(dto.getSender())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .source(dto.getSource())
                .timeSent(dto.getTimeSent())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private CommunicationLogDto mapToResource(@NonNull final CommunicationLog entity) {
        return CommunicationLogDto.builder()
                .id(entity.getId())
                .chainLinkId(entity.getChainLinkId())
                .sender(entity.getSender())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .source(entity.getSource())
                .timeSent(entity.getTimeSent())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    private CommunicationLogDto mapToResource(@NonNull final CommunicationLog entity,
                                              @NonNull final List<CommunicationRecipientDto> recipients) {
        return CommunicationLogDto.builder()
                .id(entity.getId())
                .chainLinkId(entity.getChainLinkId())
                .sender(entity.getSender())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .source(entity.getSource())
                .timeSent(entity.getTimeSent())
                .recipients(recipients)
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }
}
