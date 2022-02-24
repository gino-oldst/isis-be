package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.AlertPolicy;

import java.util.Optional;

public interface AlertPolicyRepository extends JpaRepository<AlertPolicy, String>,
        JpaSpecificationExecutor<AlertPolicy> {

    Optional<AlertPolicy> findByName(@NonNull final String name);

    Optional<AlertPolicy> findByInternalCode(@NonNull final String internalCode);
}
