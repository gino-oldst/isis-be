package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricDataDto implements Dto{

    private static final long serialVersionUID = 4069393566239549073L;

    private String id;

    private String userId;

    private LocalDate dataDate;

    private LocalDate dateImported;

    private LocalDate dateExpiry;

    private boolean expired;

    private String source;

    private String historicDataStatus;
}