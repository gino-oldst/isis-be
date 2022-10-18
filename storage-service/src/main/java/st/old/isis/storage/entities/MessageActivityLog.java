package st.old.isis.storage.entities;

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
import java.time.LocalDateTime;

@Data
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageActivityLog implements Serializable {

  private static final long serialVersionUID = 1105173063055870983L;

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String messageId;

  private String alertId;

  private String executorId;

  private String event;

  @Enumerated(EnumType.STRING)
  private ActivityType type;

  @Enumerated(EnumType.STRING)
  private ActivityTargetType target;

  @CreationTimestamp private LocalDateTime dateCreated;

  @UpdateTimestamp private LocalDateTime dateModified;
}
