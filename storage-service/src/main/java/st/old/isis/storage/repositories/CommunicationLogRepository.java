package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.CommunicationLog;

public interface CommunicationLogRepository extends JpaRepository<CommunicationLog, String>,
        JpaSpecificationExecutor<CommunicationLog> {
}
