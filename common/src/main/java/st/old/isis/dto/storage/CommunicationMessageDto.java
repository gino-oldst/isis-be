package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationMessageDto implements Dto {

    private static final long serialVersionUID = 1391943642716682739L;

    private String id;

    private String chainLinkId;

    private String message;

    private String sourceMessageId;

    private LocalDateTime timeSent;

    private String sender;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;

}
