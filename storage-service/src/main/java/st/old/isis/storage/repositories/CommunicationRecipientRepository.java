package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.CommunicationRecipient;

public interface CommunicationRecipientRepository extends JpaRepository<CommunicationRecipient, String>,
        JpaSpecificationExecutor<CommunicationRecipient> {
}
