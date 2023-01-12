package st.old.isis.dto.util;

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
public class ProcessRequestDto implements Dto {

    private static final long serialVersionUID = 5843563067951306780L;

    private String source;

    private LocalDate importDate;

    private LocalDate dateFrom;

    private LocalDate dateTo;

}
