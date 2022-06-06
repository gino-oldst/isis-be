package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.AlertStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAlertNotificationDto implements Dto {

    private static final long serialVersionUID = 1833700063653215964L;

    private String commChainLinkId;

    private String commMessageId;

    private String message;

    private String policyId;

    private String policyName;

    private LocalDate importDate;

    private int count;

}
