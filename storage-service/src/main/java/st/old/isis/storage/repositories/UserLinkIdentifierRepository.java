package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.UserLinkIdentifier;

import java.util.List;

public interface UserLinkIdentifierRepository extends JpaRepository<UserLinkIdentifier, String>,
        JpaSpecificationExecutor<UserLinkIdentifier> {

    List<UserLinkIdentifier> findAllByUserLinkId(String userLinkId);
}
