package st.old.isis.extraction.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import st.old.isis.enums.ProcessedStatus;

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
public class FileDownloadHistory implements Serializable {

  private static final long serialVersionUID = 1870551434888726136L;

  @Id private String id;

  private String filePath;

  private LocalDate importDate;

  @Enumerated(EnumType.STRING)
  private ProcessedStatus status;

  @CreationTimestamp private LocalDateTime dateCreated;
}
