package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.MessageBypassType;
import st.old.isis.enums.SourceType;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCommunicationDto implements Dto {

    private static final long serialVersionUID = -1921228757808378585L;

    private String alertID;

    private String message;

    private String subject;

    private List<String> senders;

    private List<String> recipients;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private SourceType sourceType;

    private MessageBypassType bypassType;

}
