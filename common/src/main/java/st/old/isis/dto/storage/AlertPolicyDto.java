package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.AlertPolicyStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertPolicyDto implements Dto {

    private static final long serialVersionUID = 7888918429149225853L;

    private String id;

    private String name;

    private String internalCode;

    private String description;

    private AlertPolicyStatus status;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;
}
