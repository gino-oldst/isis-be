package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationRecipient;
import st.old.isis.storage.repositories.CommunicationRecipientRepository;
import st.old.isis.storage.specifications.CommunicationRecipientSpecification;
import st.old.isis.storage.validators.CommunicationRecipientValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationRecipientService {

  private final CommunicationRecipientRepository repository;
  private final CommunicationRecipientValidator validator;

  public List<CommunicationRecipientDto> batchSave(
      @NonNull final List<CommunicationRecipientDto> request) {

    final var recipients =
        request.stream()
            .filter(Objects::nonNull)
            .map(CommunicationRecipientDto::getRecipient)
            .collect(Collectors.toList());

    final var filter = CommunicationRecipientDto.builder().recipients(recipients).build();
    final var spec = new CommunicationRecipientSpecification(filter);

    final var existingRecipients =
        repository.findAll(spec).stream()
            .collect(
                Collectors.toMap(
                    CommunicationRecipient::getRecipient,
                    (recipient) -> new ArrayList<>(List.of(recipient.getChainLinkId())),
                    (old, latest) -> {
                      old.addAll(latest);
                      return old;
                    }));

    final var recipientsEntity =
        request.stream()
            .filter(recipient -> !Strings.isEmpty(recipient.getRecipient()))
            .filter(
                recipient ->
                    !existingRecipients
                        .getOrDefault(recipient.getRecipient(), new ArrayList<>())
                        .contains(recipient.getChainLinkId()))
            .map(this::mapToEntity)
            .collect(Collectors.toList());

    return repository.saveAll(recipientsEntity).stream()
        .map(this::mapToResource)
        .collect(Collectors.toList());
  }

  @Audited(event = "CREATE_COMMUNICATION_RECIPIENT")
  public CommunicationRecipientDto create(@NonNull final CommunicationRecipientDto dto) {

    final var entity = mapToEntity(dto);

    final var createdEntity = repository.save(entity);

    return mapToResource(createdEntity);
  }

  @Audited(event = "UPDATE_COMMUNICATION_RECIPIENT")
  public CommunicationRecipientDto update(@NonNull final CommunicationRecipientDto dto)
      throws IsisException {

    final var oldEntity = findById(dto.getId());

    validator.updateValidate(oldEntity, dto);

    final var newEntity = mapToEntity(dto);

    final var updatedEntity = repository.save(newEntity);

    return mapToResource(updatedEntity);
  }

  @Audited(event = "GET_COMMUNICATION_RECIPIENT_BY_ID")
  public CommunicationRecipientDto getById(@NonNull final String id) throws IsisException {

    final var entity = findById(id);

    return mapToResource(entity);
  }

  @Audited(event = "DELETE_COMMUNICATION_RECIPIENT")
  public void delete(@NonNull @Audited final String id) throws IsisException {

    final var entity = findById(id);

    repository.delete(entity);
  }

  @Audited(event = "SEARCH_COMMUNICATION_RECIPIENT")
  public ListDto<CommunicationRecipientDto> search(
      @NonNull @Audited final CommunicationRecipientDto filter) {

    final var spec = new CommunicationRecipientSpecification(filter);

    final var results =
        repository.findAll(spec).stream().map(this::mapToResource).collect(Collectors.toList());

    final var size = results.size();

    return ListDto.<CommunicationRecipientDto>builder()
        .data(results)
        .totalNoOfRecords(size)
        .totalPages(size)
        .totalCurrentElements(size)
        .build()
        .success();
  }

  @Audited(event = "SEARCH_COMMUNICATION_RECIPIENT")
  public ListDto<CommunicationRecipientDto> search(
      @NonNull @Audited final CommunicationRecipientDto filter, @Audited Pageable pageable) {

    final var spec = new CommunicationRecipientSpecification(filter);

    final var searchResults = repository.findAll(spec, pageable);

    final var results =
        searchResults.stream().map(this::mapToResource).collect(Collectors.toList());

    return ListDto.<CommunicationRecipientDto>builder()
        .data(results)
        .totalNoOfRecords(searchResults.getTotalElements())
        .totalPages(searchResults.getTotalPages())
        .totalCurrentElements(searchResults.getNumberOfElements())
        .build()
        .success();
  }

  private CommunicationRecipient findById(@NonNull final String id) throws IsisException {
    return repository
        .findById(id)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find a recipient with id: [%s]", id),
                    "NotFoundException"));
  }

  private CommunicationRecipient mapToEntity(@NonNull final CommunicationRecipientDto dto) {
    return CommunicationRecipient.builder()
        .id(dto.getId())
        .chainLinkId(dto.getChainLinkId())
        .recipient(dto.getRecipient())
        .dateImported(dto.getDateImported())
        .metadata(dto.getMetadata())
        .build();
  }

  private CommunicationRecipientDto mapToResource(@NonNull final CommunicationRecipient entity) {
    return CommunicationRecipientDto.builder()
        .id(entity.getId())
        .chainLinkId(entity.getChainLinkId())
        .recipient(entity.getRecipient())
        .dateImported(entity.getDateImported())
        .metadata(entity.getMetadata())
        .build();
  }
}
