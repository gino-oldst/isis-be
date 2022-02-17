package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.Role;

import java.util.List;

public interface RoleRepository
    extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {

  List<Role> findAllByUserId(@NonNull final String userId);
}
