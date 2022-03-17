package st.old.isis.notification.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory implements Serializable {

  private static final long serialVersionUID = -4041323892105446912L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String subject;

  @Column(columnDefinition = "TEXT")
  private String message;

  private String recipient;

  @CreationTimestamp private LocalDateTime dateSent;
}
