package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.EscalationHistory;


public interface EscalationHistoryRepository extends JpaRepository<EscalationHistory, String>, JpaSpecificationExecutor<EscalationHistory> {

}
