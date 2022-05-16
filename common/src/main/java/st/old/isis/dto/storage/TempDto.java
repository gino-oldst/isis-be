package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempDto implements Dto {
  private static final long serialVersionUID = 1847357710230304161L;

  private String fileSource;
}
