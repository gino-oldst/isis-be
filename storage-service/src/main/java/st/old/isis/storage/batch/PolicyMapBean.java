package st.old.isis.storage.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.KeywordDto;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyMapBean {

    private Map<AlertPolicyDto, List<KeywordDto>> policyMap;

}
