package st.old.isis.storage.batch;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.AlertPolicyService;
import st.old.isis.storage.services.KeywordService;
import st.old.isis.storage.services.PolicyMapService;
import st.old.isis.utils.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Data
public class PolicyMapBeanService {
    @Autowired private AlertPolicyService alertPolicyService;
    @Autowired private PolicyMapService policyMapService;
    @Autowired private KeywordService keywordService;

    @JobScope
    private Map<AlertPolicyDto, List<KeywordDto>> policyMap = new HashMap<>();
    public Map<AlertPolicyDto, List<KeywordDto>> initialize() throws IsisException {

        log.debug("Initializing policy map for batch processing alerts");

        Map<AlertPolicyDto, List<KeywordDto>> policyMap = new HashMap<>();

        final var listOfPolicies = alertPolicyService.search(AlertPolicyDto.builder().build());

        if (listOfPolicies == null || ListUtils.isNullOrEmpty(listOfPolicies.getData())) {
            log.warn("No Policy Records Found");
            throw IsisException.notFound("No Policy Records Found", "NotFoundException");
        }

        for(AlertPolicyDto policy : listOfPolicies.getData()) {

            final var policyId = policy.getId();

            final var listPolicyMap = policyMapService.search(PolicyMapDto.builder().policyId(policyId).build());

            if(listPolicyMap == null || ListUtils.isNullOrEmpty(listPolicyMap.getData())){
                log.warn("No policy map keywords found for policy {}", policyId);
                throw IsisException.notFound(
                        String.format("No policy map keywords found for policy [%s]", policyId),
                        "NotFoundException");
            }

            final var listOfKeywords = listPolicyMap.getData().stream()
                    .map(p -> {
                        try {
                            return keywordService.getById(p.getKeywordId());
                        } catch (IsisException e) {
                            log.warn(e.getMessage());
                            return null;
                        }})
                    .filter(word -> word != null)
                    .collect(Collectors.toList());

            policyMap.put(policy, listOfKeywords);
        }

        return policyMap;
    }

}
