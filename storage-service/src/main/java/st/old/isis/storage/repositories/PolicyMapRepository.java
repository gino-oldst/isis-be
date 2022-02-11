package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.PolicyMap;

import java.util.Optional;

public interface PolicyMapRepository extends JpaRepository<PolicyMap, String>,
        JpaSpecificationExecutor<PolicyMap> {

    Optional<PolicyMap> findByKeywordIdAndPolicyId(@NonNull final String keywordId,
                                                   @NonNull final String policyId);
}
