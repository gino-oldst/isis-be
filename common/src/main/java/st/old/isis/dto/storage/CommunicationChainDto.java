package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationChainDto implements Dto {

    private static final long serialVersionUID = 8815849146610375028L;

    private String id;

    private String source;

    private String sourceChainId;

    private String subject;

    private LocalDate datePeriod;

    private LocalDate dateImported;

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;

    private List<CommunicationMessageDto> messages;

    private List<CommunicationRecipientDto> recipients;

    // Used for search function
    private List<String> chains;

    private LocalDate filterDateFrom;

    private LocalDate filterDateTo;
}
