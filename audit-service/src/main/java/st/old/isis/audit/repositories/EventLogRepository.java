package st.old.isis.audit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.audit.entities.EventLog;

public interface EventLogRepository
    extends JpaRepository<EventLog, String>, JpaSpecificationExecutor<EventLog> {}
