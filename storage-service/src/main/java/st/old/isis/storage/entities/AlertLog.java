package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import st.old.isis.enums.AlertStatusType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    indexes = {
            @Index(name = "idx_communication_message_commMessageId", columnList = "commMessageId"),
            @Index(name = "idx_communication_message_keywordId", columnList = "keywordId"),
            @Index(name = "idx_communication_message_status", columnList = "status")
    })
public class AlertLog implements Serializable {

    private static final long serialVersionUID = -6826325590367273222L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String commChainLinkId;

    private String commMessageId;

    @Lob
    private String message;

    private String policyId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String keyword;

    private String keywordId;

    private LocalDate importDate;

    private LocalDateTime timeSent;

    private String sender;

    private String source;

    @Enumerated(EnumType.STRING)
    private AlertStatusType status;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    private LocalDateTime dateModified;

    private boolean deleted;
}
