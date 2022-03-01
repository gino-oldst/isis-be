package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.AlertStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertLogDto implements Dto {

    private static final long serialVersionUID = -4951611226699230223L;

    private String id;

    private String commChainLinkId;

    private String commMessageId;

    private String policyId;

    private String description;

    private String notes;

    private String keyword;

    private String keywordId;

    private String assignedUserId;

    private AlertStatusType status;

    private LocalDate alertDate;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;
}
