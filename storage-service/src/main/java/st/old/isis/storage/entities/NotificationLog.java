package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog implements Serializable {

    private static final long serialVersionUID = 1693122546876733821L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String userId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean seen;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    private boolean deleted;
}
