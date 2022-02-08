package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationLog implements Serializable {

    private static final long serialVersionUID = 3476394832337716307L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chainLinkId;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timeSent;

    private Long sender;

    private String source;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    private LocalDateTime dateModified;

    private boolean deleted;
}
