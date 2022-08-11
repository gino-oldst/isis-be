package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.ProcessedStatus;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataImportRecordDto implements Dto {

    private static final long serialVersionUID = 5917804641545769873L;

    private String id;

    private String source;

    private LocalDate importDate;

    private ProcessedStatus processedStatus;

}
