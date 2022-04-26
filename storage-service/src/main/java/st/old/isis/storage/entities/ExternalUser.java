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
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    indexes = {
      @Index(name = "ext_user_ext_id_idx", columnList = "externalId,source", unique = true),
      @Index(name = "idx_external_user_email", columnList = "email"),
      @Index(name = "idx_external_user_firstname", columnList = "firstName"),
      @Index(name = "idx_external_user_lastname", columnList = "lastName"),
      @Index(name = "idx_external_user_source", columnList = "source")
    })
public class ExternalUser implements Serializable {

  private static final long serialVersionUID = -9127926173004611160L;

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  private String externalId;

  private String email;

  private String phoneNumber;

  private String firstName;

  private String lastName;

  private String type;

  private String source;

  @CreationTimestamp private LocalDateTime dateCreated;

  @UpdateTimestamp private LocalDateTime dateModified;

  private boolean deleted;
}
