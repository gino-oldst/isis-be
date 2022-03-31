package st.old.isis.storage.entities;

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
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationMessage implements Serializable {

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
}
