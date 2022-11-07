package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import st.old.isis.enums.MessageBypassType;
import st.old.isis.enums.PatternType;

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
            @Index(name = "idx_pattern_bypass_pattern_type", columnList = "patternType"),
            @Index(name = "idx_pattern_bypass_bypass_type", columnList = "bypassType")
    }
)
public class PatternBypass implements Serializable {
    
    private static final long serialVersionUID = -1221528702178302719L;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    
    private String value;

    @Enumerated(EnumType.STRING)
    private PatternType patternType;

    @Enumerated(EnumType.STRING)
    private MessageBypassType bypassType;

    private String targetMessageTypes;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    private LocalDateTime dateModified;

    private boolean deleted;

}
