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
@Table(indexes = {
        @Index(name = "idx_escalation_history_alert_id", columnList = "alertId"),
        @Index(name = "idx_escalation_history_date_created", columnList = "date_created")
})
public class AssignToUserHistory implements Serializable {

    private static final long serialVersionUID = -578391310040170498L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String assignedFrom;

    private String assignedTo;

    private String executorId;

    private String alertId;

    @Column(name = "date_created")
    @CreationTimestamp
    private LocalDateTime dateCreated;

    @Column(name = "date_modified")
    @UpdateTimestamp
    private LocalDateTime dateModified;


}
