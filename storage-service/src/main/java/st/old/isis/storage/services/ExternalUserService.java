package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.ExternalUser;
import st.old.isis.storage.entities.temp.TempExternalUser;
import st.old.isis.storage.repositories.ExternalUserRepository;
import st.old.isis.storage.repositories.temp.TempExternalUserRepository;
import st.old.isis.storage.specifications.ExternalUserSpecification;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalUserService {

  private final ExternalUserRepository repository;
  private final TempExternalUserRepository tempRepository;

  @Audited(event = "BATCH_SAVE_EXTERNAL_USER")
  public ListDto<ExternalUserDto> batchedSave(@NonNull final List<ExternalUserDto> list) {

    final var users =
        list.stream()
            .filter(user -> !Strings.isEmpty(user.getEmail()))
            .collect(Collectors.toMap(this::mapKey, Function.identity(), (v1, v2) -> v1))
            .values()
            .stream()
            .map(this::mapToTempEntity)
            .collect(Collectors.toList());

    final List<ExternalUserDto> createdUsers =
        tempRepository.saveAll(users).stream()
            .map(this::mapToResource)
            .collect(Collectors.toList());

    final int size = createdUsers.size();

    return ListDto.<ExternalUserDto>builder()
        .data(createdUsers)
        .totalNoOfRecords(size)
        .totalPages(1)
        .totalCurrentElements(size)
        .build()
        .success();
  }

  @Audited(event = "FINALIZE_BATCH_SAVE_EXTERNAL_USER")
  public void finalizeBatch(@Audited String fileSource) {

    log.info("Start merging users from [{}]", fileSource);

    final long start = System.currentTimeMillis();

    log.info("Users added: {}", repository.merge(fileSource));
    log.info("Temporary users deleted: {}", tempRepository.deleteAllByFileSource(fileSource));

    log.info("Merge users time elapsed: {} ms", System.currentTimeMillis() - start);
  }

  @Audited(event = "DELETE_TEMP_EXTERNAL_USER")
  public void deleteTempExternalUser(@Audited String fileSource) {

    log.info("Start deleting temp users from [{}]", fileSource);

    final long start = System.currentTimeMillis();

    log.info("Temporary users deleted: {}", tempRepository.deleteAllByFileSource(fileSource));

    log.info("Delete temp users time elapsed: {} ms", System.currentTimeMillis() - start);
  }

  @Audited(event = "DELETE_EXTERNAL_USER")
  public void delete(@NonNull @Audited final String id) throws IsisException {

    final var user = findById(id);

    repository.delete(user);
  }

  @Audited(event = "GET_EXTERNAL_USER_BY_ID")
  public ExternalUserDto getById(@NonNull @Audited final String id) throws IsisException {

    final var entity = findById(id);

    return mapToResource(entity);
  }

  @Audited(event = "GET_EXTERNAL_USER_BY_EMAIL")
  public ExternalUserDto getByEmail(@NonNull @Audited final String email) throws IsisException {

    final var entity = findByEmail(email);

    return mapToResource(entity);
  }

  @Audited(event = "SEARCH_EXTERNAL_USER")
  public ListDto<ExternalUserDto> search(
      @NonNull @Audited final ExternalUserDto filter, Sort sort) {

    final var spec = new ExternalUserSpecification(filter);

    final var results =
        repository.findAll(spec, sort).stream()
            .map(this::mapToResource)
            .collect(Collectors.toList());

    final var size = results.size();

    return ListDto.<ExternalUserDto>builder()
        .data(results)
        .totalNoOfRecords(size)
        .totalPages(size)
        .totalCurrentElements(size)
        .build()
        .success();
  }

  @Audited(event = "SEARCH_EXTERNAL_USER")
  public ListDto<ExternalUserDto> search(
      @NonNull @Audited final ExternalUserDto filter, @Audited Pageable pageable) {

    final var spec = new ExternalUserSpecification(filter);

    final var searchResults = repository.findAll(spec, pageable);

    final var results =
        searchResults.stream().map(this::mapToResource).collect(Collectors.toList());

    return ListDto.<ExternalUserDto>builder()
        .data(results)
        .totalNoOfRecords(searchResults.getTotalElements())
        .totalPages(searchResults.getTotalPages())
        .totalCurrentElements(searchResults.getNumberOfElements())
        .build()
        .success();
  }

  private List<ExternalUser> findAll(@NonNull final ExternalUserDto filter) {

    final var spec = new ExternalUserSpecification(filter);

    return repository.findAll(spec);
  }

  private ExternalUser findById(@NonNull final String id) throws IsisException {
    return repository
        .findById(id)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find external user with id: [%s]", id),
                    "NotFoundException"));
  }

  private ExternalUser findByEmail(@NonNull final String email) throws IsisException {
    return repository
            .findByEmail(email)
            .orElseThrow(
                    () ->
                            IsisException.notFound(
                                    String.format("Cannot find external user with id: [%s]", email),
                                    "NotFoundException"));
  }

  private ExternalUser findByExternalId(@NonNull final String externalId) throws IsisException {
    return repository
        .findByExternalId(externalId)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find user with external id: [%s]", externalId),
                    "NotFoundException"));
  }

  private ExternalUser mapToEntity(@NonNull final ExternalUserDto dto) {
    return ExternalUser.builder()
        .id(dto.getId())
        .externalId(dto.getExternalId())
        .email(dto.getEmail())
        .phoneNumber(dto.getPhoneNumber())
        .firstName(dto.getFirstName())
        .lastName(dto.getLastName())
        .source(dto.getSource())
        .type(dto.getType())
        .deleted(dto.isDeleted())
        .build();
  }

  private TempExternalUser mapToTempEntity(@NonNull final ExternalUserDto dto) {
    return TempExternalUser.builder()
        .id(dto.getId())
        .externalId(dto.getExternalId())
        .email(dto.getEmail())
        .phoneNumber(dto.getPhoneNumber())
        .firstName(dto.getFirstName())
        .lastName(dto.getLastName())
        .source(dto.getSource())
        .type(dto.getType())
        .deleted(dto.isDeleted())
        .fileSource(dto.getFileSource())
        .build();
  }

  private ExternalUserDto mapToResource(@NonNull final ExternalUser entity) {
    return ExternalUserDto.builder()
        .id(entity.getId())
        .externalId(entity.getExternalId())
        .email(entity.getEmail())
        .phoneNumber(entity.getPhoneNumber())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .source(entity.getSource())
        .type(entity.getType())
        .deleted(entity.isDeleted())
        .build();
  }

  private ExternalUserDto mapToResource(@NonNull final TempExternalUser entity) {
    return ExternalUserDto.builder()
        .id(entity.getId())
        .externalId(entity.getExternalId())
        .email(entity.getEmail())
        .phoneNumber(entity.getPhoneNumber())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .source(entity.getSource())
        .type(entity.getType())
        .deleted(entity.isDeleted())
        .build();
  }

  private String mapKey(@NonNull final ExternalUserDto user) {
    return String.format("%s-%s", user.getExternalId(), user.getSource()).toLowerCase();
  }
}
