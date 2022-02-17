package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.CommunicationChain;

public interface CommunicationChainRepository extends JpaRepository<CommunicationChain, String>,
        JpaSpecificationExecutor<CommunicationChain> {
}
