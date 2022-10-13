package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.ActivityTargetType;
import st.old.isis.enums.ActivityType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageActivityLogDto implements Dto {

    private static final long serialVersionUID = -4728768962803282762L;

    private String id;

    private String messageId;

    private String alertId;

    private String executorId;

    private String event;

    private ActivityType type;

    private ActivityTargetType target;

    private LocalDateTime dateCreated;

    //search related fields
    private LocalDate dateCreatedFrom;
    private LocalDate dateCreatedTo;
}
