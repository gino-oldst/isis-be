package st.old.isis.dto.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.enums.AlertStatusType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickCommunicationDto implements Dto {

    private static final long serialVersionUID = -1184001885573580486L;

    private CommunicationMessageDto message;

    private ListDto<AlertLogDto> alerts;

    private ListDto<CommunicationRecipientDto> recipients;

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private List<String> senders;

    private List<String> keywords;

    private List<String> keywordIds;

    private List<String> sources;

    private List<AlertStatusType> statusTypes;


}
