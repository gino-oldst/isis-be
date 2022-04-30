package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.dto.metadata.Metadata;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationRecipientDto implements Dto {

    private static final long serialVersionUID = 8815849146610375028L;

    private String id;

    private String chainLinkId;

    private String recipient;

    private List<String> recipients;

    private LocalDate dateImported;

    // Used for search function
    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Metadata metadata;

    private List<String> chains;
}
