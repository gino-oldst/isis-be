package st.old.isis.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryDto implements Dto {

  private static final long serialVersionUID = -3617516800457997804L;

  private String subject;

  private String message;

  private String recipient;

  private LocalDateTime dateSent;
}
