package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.storage.entities.User;

public interface UserRepository extends JpaRepository<User, String>,
        JpaSpecificationExecutor<User> {
}
