package st.old.isis.storage.entities.temp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import st.old.isis.dto.metadata.Metadata;
import st.old.isis.mappers.MetadataConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    indexes = {
      @Index(name = "idx_temp_communication_recipient_recipient", columnList = "recipient"),
      @Index(name = "idx_temp_communication_recipient_chain_link_id", columnList = "chainLinkId")
    })
public class TempCommunicationRecipient implements Serializable {

  private static final long serialVersionUID = -8705611630590352240L;

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String chainLinkId;

  private String recipient;

  private LocalDate dateImported;

  private String fileSource;

  @Lob
  @Convert(converter = MetadataConverter.class)
  private Metadata metadata;
}
