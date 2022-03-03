package st.old.isis.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLogDto implements Dto {

  private static final long serialVersionUID = -1319314257915092582L;

  private String txnId;

  private String source;

  private List<AuditEvent> events;
}
