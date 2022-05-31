package st.old.isis.storage.entities.temp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import st.old.isis.dto.metadata.Metadata;
import st.old.isis.mappers.MetadataConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    indexes = {
      @Index(name = "idx_temp_communication_message_file_source", columnList = "fileSource"),
      @Index(name = "idx_communication_message_time_sent", columnList = "timeSent"),
      @Index(name = "idx_communication_message_source", columnList = "source"),
      @Index(name = "idx_communication_message_sender", columnList = "sender"),
      @Index(name = "idx_communication_message_chain_link_id", columnList = "chainLinkId")
    })
public class TempCommunicationMessage implements Serializable {

  private static final long serialVersionUID = 3476394832337716307L;

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String chainLinkId;

  private String subject;

  @Lob private String message;

  private String sourceMessageId;

  private LocalDateTime timeSent;

  private LocalDate importDate;

  private String sender;

  private String source;

  private String type;

  @Lob
  @Convert(converter = MetadataConverter.class)
  private Metadata metadata;

  @CreationTimestamp private LocalDateTime dateCreated;

  @UpdateTimestamp private LocalDateTime dateModified;

  private boolean deleted;

  private String fileSource;
}
