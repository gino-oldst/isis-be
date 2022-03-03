package st.old.isis.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

  private String event;

  private String description;

  private String metadata;

  private LocalDateTime dateCreated;

  private LocalDateTime dateModified;
}
