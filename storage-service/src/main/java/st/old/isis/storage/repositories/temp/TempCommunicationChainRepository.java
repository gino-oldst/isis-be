package st.old.isis.storage.repositories.temp;

import org.springframework.data.jpa.repository.JpaRepository;
import st.old.isis.storage.entities.temp.TempCommunicationChain;

public interface TempCommunicationChainRepository
    extends JpaRepository<TempCommunicationChain, String> {}
