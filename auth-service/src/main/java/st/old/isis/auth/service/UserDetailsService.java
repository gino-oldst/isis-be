package st.old.isis.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import st.old.isis.auth.service.internal.StorageService;
import st.old.isis.dto.storage.RoleDto;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service(value = "userDetailsService")
public class UserDetailsService
    implements org.springframework.security.core.userdetails.UserDetailsService {

  private final StorageService storageService;

  @Override
  public UserDetails loadUserByUsername(@NonNull final String username)
      throws UsernameNotFoundException {

    try {
      final var user = storageService.getByUsername(username);
      return new org.springframework.security.core.userdetails.User(
          user.getUsername(),
          user.getPassword(),
          !user.isDeleted(),
          true,
          true,
          true,
          getAuthority(user.getRoles()));
    } catch (Exception e) {
      throw new UsernameNotFoundException("Invalid username or password:", e);
    }
  }

  private Set<SimpleGrantedAuthority> getAuthority(List<RoleDto> roles) {
    return roles.stream()
        .map(RoleDto::getRole)
        .map(Enum::name)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }
}
