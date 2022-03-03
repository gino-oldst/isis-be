package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.AppUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.utils.BCryptEncoderService;
import st.old.isis.storage.entities.AppUser;
import st.old.isis.storage.repositories.AppUserRepository;
import st.old.isis.storage.validators.AppUserValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserService {

  private final AppUserRepository repository;
  private final AppUserValidator validator;
  private final BCryptEncoderService encoderService;
  private final RoleService roleService;

  public AppUserDto create(@NonNull final AppUserDto dto) throws IsisException {

    validator.createValidate(dto);

    final var entity = mapToEntity(dto);

    entity.setPassword(encoderService.encodeToBcrypt(dto.getPassword()));

    final var createdEntity = repository.save(entity);

    final var roles = dto.getRoles();
    final var hasRoles = roles != null && !roles.isEmpty();

    if (hasRoles) {
      roleService.save(createdEntity.getId(), roles);
    }

    return mapToResource(createdEntity);
  }

  public AppUserDto update(@NonNull final AppUserDto dto) throws IsisException {

    final var oldEntity = findById(dto.getId());

    validator.updateValidate(oldEntity, dto);

    final var newEntity = mapToEntity(oldEntity, dto);

    final var updatedEntity = repository.save(newEntity);

    final var roles = dto.getRoles();
    final var hasRoles = roles != null && !roles.isEmpty();

    if (hasRoles) {
      roleService.save(updatedEntity.getId(), roles);
    }

    return mapToResource(updatedEntity);
  }

  public AppUserDto getById(@NonNull final String id) throws IsisException {

    final var entity = findById(id);

    return mapToResource(entity);
  }

  public AppUserDto getByUsername(@NonNull final String username) throws IsisException {

    final var entity = findByUsername(username);

    return mapToResource(entity);
  }

  public void delete(@NonNull final String id) throws IsisException {

    final var entity = findById(id);

    repository.delete(entity);
  }

  public AppUserDto getAuthenticationDetails(@NonNull final String username) throws IsisException {

    final var user = findByUsername(username);

    final var dto = mapToResource(user);

    dto.setPassword(user.getPassword());
    dto.setRoles(roleService.getAllByUserId(user.getId()));

    return dto;
  }

  private AppUser findById(@NonNull final String id) throws IsisException {
    return repository
        .findById(id)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find app user with id: [%s]", id), "NotFoundException"));
  }

  private AppUser findByUsername(@NonNull final String username) throws IsisException {
    return repository
        .findByUsername(username)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find app user with username: [%s]", username),
                    "NotFoundException"));
  }

  private AppUser mapToEntity(@NonNull final AppUserDto dto) {
    return AppUser.builder()
        .id(dto.getId())
        .username(dto.getUsername())
        .dateCreated(dto.getDateCreated())
        .dateModified(dto.getDateModified())
        .deleted(dto.isDeleted())
        .build();
  }

  private AppUserDto mapToResource(@NonNull final AppUser entity) {
    return AppUserDto.builder()
        .id(entity.getId())
        .username(entity.getUsername())
        .dateCreated(entity.getDateCreated())
        .dateModified(entity.getDateModified())
        .deleted(entity.isDeleted())
        .build();
  }

  // map read-only properties from old entity to new entity
  private AppUser mapToEntity(@NonNull AppUser oldEntity, @NonNull AppUserDto newDto) {

    final var entity = mapToEntity(newDto);
    entity.setDateCreated(oldEntity.getDateCreated());
    entity.setPassword(oldEntity.getPassword());

    return entity;
  }
}
