package st.old.isis.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.NotificationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientDto implements Dto {

  private static final long serialVersionUID = 3531031457700337378L;

  private NotificationType type;

  private String destination;

  private String recipientId;
}
