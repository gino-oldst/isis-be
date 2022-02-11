package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.AlertLog;

public interface AlertLogRepository extends JpaRepository<AlertLog, String>,
        JpaSpecificationExecutor<AlertLog> {
}
