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
public class AlertLogDto implements Dto {

    private static final long serialVersionUID = -4951611226699230223L;

    private String id;

    private String commChainLinkId;

    private String commMessageId;

    private String message;

    private String policyId;

    private String description;

    private String notes;

    private String keyword;

    private String keywordId;

    private String assignedUserId;

    private AlertStatusType status;

    private LocalDate importDate;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private LocalDateTime dateCreatedFromRange;

    private LocalDateTime dateCreatedToRange;

    private List<String> keywordIds;

    private List<AlertStatusType> statusTypes;

    private boolean deleted;
}
