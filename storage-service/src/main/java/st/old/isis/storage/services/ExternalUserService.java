package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.ExternalUser;
import st.old.isis.storage.repositories.ExternalUserRepository;
import st.old.isis.storage.specifications.ExternalUserSpecification;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalUserService {

  private final ExternalUserRepository repository;

  @Audited(event = "BATCH_SAVE_EXTERNAL_USER")
  public ListDto<ExternalUserDto> batchedSave(@NonNull final List<ExternalUserDto> list) {

    final var existingUserMap = getExistingUserMap(list);

    final List<ExternalUser> users =
        list.stream()
            .map(this::mapToEntity)
            .peek(user -> user.setExternalId(user.getExternalId().toLowerCase()))
            .peek(
                user ->
                    Optional.ofNullable(existingUserMap.get(mapKey(user)))
                        .ifPresent(externalUser -> user.setId(externalUser.getId())))
            .collect(Collectors.toList());

    final List<ExternalUserDto> createdUsers =
        repository.saveAll(users).stream().map(this::mapToResource).collect(Collectors.toList());

    final int size = createdUsers.size();

    return ListDto.<ExternalUserDto>builder()
        .data(createdUsers)
        .totalNoOfRecords(size)
        .totalPages(size)
        .totalCurrentElements(size)
        .build()
        .success();
  }

  @Audited(event = "DELETE_EXTERNAL_USER")
  public void delete(@NonNull @Audited final String id) throws IsisException {

    final var user = findById(id);

    repository.delete(user);
  }

  private Map<String, ExternalUser> getExistingUserMap(@NonNull final List<ExternalUserDto> list) {

    final var extIds =
        list.stream().map(ExternalUserDto::getExternalId).distinct().collect(Collectors.toList());

    final var filter = ExternalUserDto.builder().externalIds(extIds).build();

    final var results = findAll(filter);

    return results.stream()
        .collect(Collectors.toMap(this::mapKey, Function.identity()));
  }

  @Audited(event = "GET_EXTERNAL_USER_BY_ID")
  public ExternalUserDto getById(@NonNull @Audited final String id) throws IsisException {

    final var entity = findById(id);

    return mapToResource(entity);
  }

  @Audited(event = "SEARCH_EXTERNAL_USER")
  public ListDto<ExternalUserDto> search(@NonNull @Audited final ExternalUserDto filter) {

    final var results =
        findAll(filter).stream().map(this::mapToResource).collect(Collectors.toList());

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

  private String mapKey(@NonNull final ExternalUser user) {
    return String.format("%s-%s", user.getId(), user.getSource());
  }
}
