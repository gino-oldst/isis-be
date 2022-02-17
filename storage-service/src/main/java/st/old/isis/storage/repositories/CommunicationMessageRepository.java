package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.CommunicationMessage;

public interface CommunicationMessageRepository extends JpaRepository<CommunicationMessage, String>,
        JpaSpecificationExecutor<CommunicationMessage> {
}
