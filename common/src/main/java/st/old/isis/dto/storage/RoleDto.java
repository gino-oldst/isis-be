package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.UserRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto implements Dto {

  private static final long serialVersionUID = 8915610627254823931L;

  private String id;

  private String userId;

  private UserRole role;
}
