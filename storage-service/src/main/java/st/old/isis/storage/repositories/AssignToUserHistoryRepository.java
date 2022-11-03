package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.AssignToUserHistory;
import st.old.isis.storage.entities.Keyword;

import java.util.List;
import java.util.Optional;


public interface AssignToUserHistoryRepository extends JpaRepository<AssignToUserHistory, String>, JpaSpecificationExecutor<AssignToUserHistory> {
    Optional<AssignToUserHistory> findByAlertId(@NonNull final String alertId);

    Optional<AssignToUserHistory> findTop1ByAlertId(@NonNull final String alertId);

    AssignToUserHistory findFirstByAlertIdOrderByDateCreatedDesc(@NonNull final String alertId);
}
