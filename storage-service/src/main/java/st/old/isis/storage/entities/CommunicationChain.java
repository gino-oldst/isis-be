package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

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
      @Index(
          name = "idx_communication_chain_unique",
          columnList = "sourceChainId,source",
          unique = true),
      @Index(name = "idx_communication_chain_source_chain_id", columnList = "sourceChainId"),
      @Index(name = "idx_communication_chain_source", columnList = "source")
    })
public class CommunicationChain implements Serializable {

  private static final long serialVersionUID = 3476394832337716307L;

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String source;

  private String sourceChainId;

  private String subject;

  private LocalDate datePeriod;

  private LocalDate dateImported;

  private LocalDateTime dateFrom;

  private LocalDateTime dateTo;

  @CreationTimestamp private LocalDateTime dateCreated;

  @UpdateTimestamp private LocalDateTime dateModified;

  private boolean deleted;
}
