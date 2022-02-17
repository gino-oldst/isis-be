package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserDto implements Dto {

  private static final long serialVersionUID = 8915610627254823931L;

  private String id;

  private String username;

  private String password;

  private String password2;

  private LocalDateTime dateCreated;

  private LocalDateTime dateModified;

  private boolean deleted;

  private List<RoleDto> roles;
}
