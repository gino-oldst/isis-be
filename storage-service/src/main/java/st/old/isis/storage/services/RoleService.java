package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.storage.RoleDto;
import st.old.isis.storage.entities.Role;
import st.old.isis.storage.repositories.RoleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository repository;

  @Audited(event = "SAVE_ROLE")
  public List<RoleDto> save(@NonNull @Audited final String userId, @NonNull @Audited final List<RoleDto> roles) {

    deleteByUserId(userId);

    final var newRoles =
        roles.stream()
            .map(this::mapToEntity)
            .peek(role -> role.setUserId(userId))
            .collect(Collectors.toList());

    return repository.saveAll(newRoles).stream()
        .map(this::mapToResource)
        .collect(Collectors.toList());
  }

  private void deleteByUserId(@NonNull final String userId) {

    final var list = repository.findAllByUserId(userId);

    repository.deleteAll(list);
  }

  public List<RoleDto> getAllByUserId(@NonNull final String userId) {
    return repository.findAllByUserId(userId).stream()
        .map(this::mapToResource)
        .collect(Collectors.toList());
  }

  private Role mapToEntity(@NonNull final RoleDto dto) {
    return Role.builder().id(dto.getId()).userId(dto.getUserId()).role(dto.getRole()).build();
  }

  private RoleDto mapToResource(@NonNull final Role entity) {
    return RoleDto.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .role(entity.getRole())
        .build();
  }
}
