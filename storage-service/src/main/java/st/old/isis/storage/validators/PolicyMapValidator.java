package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.PolicyMap;
import st.old.isis.storage.repositories.KeywordRepository;
import st.old.isis.storage.repositories.PolicyMapRepository;
import st.old.isis.storage.services.KeywordService;

@Component
@RequiredArgsConstructor
public class PolicyMapValidator {

    private final PolicyMapRepository repository;
    private final KeywordRepository keywordRepository;

    public void validate(@NonNull final PolicyMapDto dto) throws IsisException {

        final var keywordId = dto.getKeywordId();
        final var policyId = dto.getPolicyId();

        keywordRepository.findById(keywordId);
        //todo: validate policy id

        final var optPolicyMap = repository.findByKeywordIdAndPolicyId(keywordId, policyId);

        if (optPolicyMap.isPresent()) {
            throw IsisException.conflict("Policy map already exists", "ConflictException");
        }
    }

    public void updateValidate(@NonNull final PolicyMap oldEntity,
                               @NonNull final PolicyMapDto dto) throws IsisException {

    }
}
