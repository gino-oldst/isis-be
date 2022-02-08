package st.old.isis.analysis.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import st.old.isis.enums.NotificationType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplate implements Serializable {

    private static final long serialVersionUID = 8289922420520287600L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String textTemplate;

    private String emailTemplate;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    private LocalDateTime dateModified;

    private boolean deleted;
}
