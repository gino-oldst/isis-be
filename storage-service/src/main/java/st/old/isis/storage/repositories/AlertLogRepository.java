package st.old.isis.storage.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import st.old.isis.storage.entities.AlertLog;

import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, String>,
        JpaSpecificationExecutor<AlertLog> {

    @Query("SELECT DISTINCT a.commMessageId FROM AlertLog a")
    public Page<String> findDistinctMessagesPaged(Pageable pageable);

    @Query("SELECT DISTINCT a.commMessageId FROM AlertLog a")
    public List<String> findDistinctMessages();

}
