package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignToUserHistoryDto implements Serializable, Dto {

    private static final long serialVersionUID = -8411950857435742953L;

    private String id;

    private String alertId;

    private String assignedFrom;

    private String assignedTo;

    private String executorId;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private String responseStatus;

    private String exception;

    private String exceptionMessage;

    private LocalDateTime dateCreatedFromRange;

    private LocalDateTime dateCreatedToRange;
}
