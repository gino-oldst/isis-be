package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.Escalation;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationHistoryDto implements Serializable, Dto {

    private static final long serialVersionUID = -2411950857430745953L;

    private String id;

    private String alertId;

    private Escalation escalatedFrom;

    private Escalation escalatedTo;

    private String executorId;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private String responseStatus;

    private String exception;

    private String exceptionMessage;

    private LocalDateTime dateCreatedFromRange;

    private LocalDateTime dateCreatedToRange;
}
