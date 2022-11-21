package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.UserLink;

import java.util.Optional;

public interface UserLinkRepository extends JpaRepository<UserLink, String>,
        JpaSpecificationExecutor<UserLink> {

    Optional<UserLink> findByUserIdAndExternalUserId(@NonNull final String userId,
                                                     @NonNull final String externalUserId);

    Optional<UserLink> findByFirstNameAndLastNameAndCompany(@NonNull final String firstName,
                                                     @NonNull final String lastName, @NonNull final String company);
}
