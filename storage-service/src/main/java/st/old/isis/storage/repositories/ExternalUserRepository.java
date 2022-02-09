package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.ExternalUser;

import java.util.Optional;

public interface ExternalUserRepository extends JpaRepository<ExternalUser, String>,
        JpaSpecificationExecutor<ExternalUser> {

    Optional<ExternalUser> findByExternalId(@NonNull final String externalId);
}
