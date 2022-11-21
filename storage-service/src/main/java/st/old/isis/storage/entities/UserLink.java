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
            @Index(name = "idx_userlink_firstname", columnList = "firstName"),
            @Index(name = "idx_userlink_lastname", columnList = "lastName"),
            @Index(name = "idx_userlink_company", columnList = "company"),
            @Index(name = "idx_userlink_externalCompanyId", columnList = "externalCompanyId")
    }
)
public class UserLink implements Serializable {

    private static final long serialVersionUID = 6076263801400084720L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String userId;

    private String externalUserId;

    private String firstName;

    private String lastName;

    private String company;

    private String externalCompanyId;

    private boolean isInternal;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    private LocalDateTime dateModified;

    private boolean deleted;
}
