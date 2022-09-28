package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.PredefinedReason;

public interface PredefinedReasonRepository extends JpaRepository<PredefinedReason, String>,
        JpaSpecificationExecutor<PredefinedReason> {

}
