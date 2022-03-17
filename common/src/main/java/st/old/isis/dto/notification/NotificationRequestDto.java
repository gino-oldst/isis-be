package st.old.isis.dto.notification;

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
public class NotificationRequestDto implements Dto {

  private static final long serialVersionUID = -2208893192617961241L;

  private String templateName;

  private MessageDto customMessage;

  private List<NotificationRecipientDto> recipients;
}
