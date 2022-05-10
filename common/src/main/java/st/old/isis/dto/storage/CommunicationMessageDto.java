package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.dto.metadata.Metadata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationMessageDto implements Dto {

  private static final long serialVersionUID = 1391943642716682739L;

  public static final Map<String, Comparator<CommunicationMessageDto>> SORT_FIELDS =
      Map.of(
          "timeSent",
          Comparator.comparing(CommunicationMessageDto::getTimeSent),
          "source",
          Comparator.comparing(CommunicationMessageDto::getSource),
          "sender",
          Comparator.comparing(CommunicationMessageDto::getSender));

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

  private LocalDateTime dateSentFrom;

  private LocalDateTime dateSentTo;

  private List<String> recipients;

  private List<String> sources;
}
