package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.dto.metadata.Metadata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationMessageDto implements Dto {

  private static final long serialVersionUID = 1391943642716682739L;

  private String id;

  private String chainLinkId;

  private String subject;

  private String message;

  private String sourceMessageId;

  private LocalDateTime timeSent;

  private LocalDate importDate;

  private String sender;

  private String source;

  private String type;

  private Metadata metadata;

  private LocalDateTime dateCreated;

  private LocalDateTime dateModified;

  private boolean deleted;

  // Used for search function
  private List<String> chains;

  private List<String> senders;

  private LocalDate dateFrom;

  private LocalDate dateTo;

  private LocalDate dateSentFrom;

  private LocalDate dateSentTo;
}
