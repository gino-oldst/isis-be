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
public class PolicyProcessorDto implements Dto {

    private static final long serialVersionUID = 8822258613259030740L;

    private String policyId;

    private LocalDate importDate;

    private String source;

    private LocalDate dateFrom;

    private LocalDate dateTo;

}
