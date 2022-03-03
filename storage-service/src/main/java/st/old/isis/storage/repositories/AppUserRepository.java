package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.AppUser;

import java.util.Optional;

public interface AppUserRepository
    extends JpaRepository<AppUser, String>, JpaSpecificationExecutor<AppUser> {

  Optional<AppUser> findByUsername(@NonNull final String username);
}
