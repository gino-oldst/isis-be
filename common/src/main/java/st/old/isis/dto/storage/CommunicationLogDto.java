package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationLogDto implements Dto {

    private static final long serialVersionUID = 8815849146610375028L;

    private String id;

    private String chainLinkId;

    private String subject;

    private String message;

    private LocalDateTime timeSent;

    private String sender;

    private String source;

    private String sourceMessageId;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;

    private List<CommunicationRecipientDto> recipients;
}
