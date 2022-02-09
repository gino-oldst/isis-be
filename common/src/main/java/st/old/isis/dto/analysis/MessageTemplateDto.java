package st.old.isis.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplateDto implements Dto {

    private static final long serialVersionUID = -8336343845400934731L;

    private String id;

    private String name;

    private String textTemplate;

    private String emailTemplate;

    private NotificationType type;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;
}
