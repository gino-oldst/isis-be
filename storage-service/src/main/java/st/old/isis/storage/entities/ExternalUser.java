package st.old.isis.storage.entities;

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
public class ExternalUser implements Serializable {

    private static final long serialVersionUID = -9127926173004611160L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalId;

    private String email;

    private String phoneNumber;

    private String firstName;

    private String lastName;

    private String type;

    private String source;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    private boolean deleted;
}
