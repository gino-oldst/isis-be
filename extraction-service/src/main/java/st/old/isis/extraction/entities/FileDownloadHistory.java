package st.old.isis.extraction.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
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

  @CreationTimestamp private LocalDateTime dateCreated;
}
