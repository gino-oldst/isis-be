package st.old.isis.extraction.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import st.old.isis.enums.ActivityTargetType;
import st.old.isis.enums.ActivityType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricData implements Serializable {

  private static final long serialVersionUID = 4069393566239549073L;
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String userId;

  private LocalDate dataDate;

  private LocalDate dateImported;

  private LocalDate dateExpiry;

  private boolean expired;

  private String source;
}
