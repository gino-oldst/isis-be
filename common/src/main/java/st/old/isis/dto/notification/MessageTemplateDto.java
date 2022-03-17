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
public class MessageTemplateDto implements Dto {

  private static final long serialVersionUID = -8336343845400934731L;

  private String id;

  private String name;

  private String subject;

  private String textTemplate;

  private String emailTemplate;

  private LocalDateTime dateCreated;

  private LocalDateTime dateModified;

  private boolean deleted;
}
