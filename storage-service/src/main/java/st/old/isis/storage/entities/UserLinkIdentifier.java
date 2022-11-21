package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import st.old.isis.enums.Source;

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
            @Index(name = "idx_userlink_id", columnList = "userLinkId"),
            @Index(name = "idx_userlink_identifier_source", columnList = "source"),
            @Index(name = "idx_userlink_identifier_import_date", columnList = "importDate"),
            @Index(name = "idx_userlink_identifier_type", columnList = "identifierType")
    }
)
public class UserLinkIdentifier implements Serializable {

    private static final long serialVersionUID = 6076263801400084720L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String userLinkId;

    private String identifier;

    private Source source;

    private String identifierId;

    private String identifierType;

    private LocalDate importDate;

    @CreationTimestamp
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    private LocalDateTime dateModified;

    private boolean deleted;
}
