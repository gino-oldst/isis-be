package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
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

    private String executorId;

    private String event;

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    @CreationTimestamp
    private LocalDateTime dateCreated;
}
