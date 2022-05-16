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
import st.old.isis.storage.entities.temp.TempCommunicationRecipient;
import st.old.isis.storage.repositories.CommunicationRecipientRepository;
import st.old.isis.storage.repositories.temp.TempCommunicationRecipientRepository;
import st.old.isis.storage.specifications.CommunicationRecipientSpecification;
import st.old.isis.storage.validators.CommunicationRecipientValidator;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationRecipientService {

  private final CommunicationRecipientRepository repository;
  private final TempCommunicationRecipientRepository tempRepository;
  private final CommunicationRecipientValidator validator;

  @Audited(event = "BATCH_RECIPIENT_SAVE")
  public List<CommunicationRecipientDto> batchSave(
      @NonNull final List<CommunicationRecipientDto> request) {

    final var recipients =
        request.stream()
            .filter(recipient -> !Strings.isEmpty(recipient.getRecipient()))
            .collect(Collectors.toMap(this::mapKey, Function.identity()))
            .values()
            .stream()
            .map(this::mapToTempEntity)
            .collect(Collectors.toList());

    return tempRepository.saveAll(recipients).stream()
        .map(this::mapToResource)
        .collect(Collectors.toList());
  }

  @Audited(event = "FINALIZE_BATCH_SAVE_RECIPIENTS")
  public void finalizeBatch(@Audited String fileSource) {

    log.info("Start merging recipients...");

    final long start = System.currentTimeMillis();

    log.info("Recipients added: {}", repository.merge(fileSource));
    log.info("Temporary recipients deleted: {}", tempRepository.deleteAllByFileSource(fileSource));

    log.info("Merge time elapsed: {} ms", System.currentTimeMillis() - start);
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

  private TempCommunicationRecipient mapToTempEntity(@NonNull final CommunicationRecipientDto dto) {
    return TempCommunicationRecipient.builder()
        .id(dto.getId())
        .chainLinkId(dto.getChainLinkId())
        .recipient(dto.getRecipient())
        .dateImported(dto.getDateImported())
        .metadata(dto.getMetadata())
        .fileSource(dto.getFileSource())
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

  private CommunicationRecipientDto mapToResource(
      @NonNull final TempCommunicationRecipient entity) {
    return CommunicationRecipientDto.builder()
        .id(entity.getId())
        .chainLinkId(entity.getChainLinkId())
        .recipient(entity.getRecipient())
        .dateImported(entity.getDateImported())
        .metadata(entity.getMetadata())
        .build();
  }

  private String mapKey(@NonNull final CommunicationRecipientDto recipient) {

    return String.format("%s-%s", recipient.getChainLinkId(), recipient.getRecipient())
        .toLowerCase();
  }
}
