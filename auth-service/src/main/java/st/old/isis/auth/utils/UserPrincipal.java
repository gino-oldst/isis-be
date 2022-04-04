package st.old.isis.auth.utils;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import st.old.isis.dto.storage.AppUserDto;
import st.old.isis.dto.storage.RoleDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

  private static final long serialVersionUID = 6794585817400941425L;

  private final AppUserDto user;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return getAuthority(user.getRoles());
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return !user.isDeleted();
  }

  private Set<SimpleGrantedAuthority> getAuthority(List<RoleDto> roles) {
    return roles.stream()
        .map(RoleDto::getRole)
        .map(Enum::name)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }
}
