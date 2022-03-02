package st.old.isis.storage.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import st.old.isis.enums.ProcessedStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "IMPORTDATE", columnList = "importDate")
        }
)
public class DataImportRecord implements Serializable {

    private static final long serialVersionUID = 1812768692357293759L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String source;

    private LocalDate importDate;

    @Enumerated(EnumType.STRING)
    private ProcessedStatus processedStatus;
}
