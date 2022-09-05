package st.old.isis.storage.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.entities.CommunicationMessage;

import java.util.List;
import java.util.stream.Stream;

public interface AlertLogRepository extends JpaRepository<AlertLog, String>,
        JpaSpecificationExecutor<AlertLog> {

    @Query("SELECT DISTINCT a.commMessageId FROM AlertLog a")
    public Page<String> findDistinctMessagesPaged(Pageable pageable);

    @Query("SELECT DISTINCT a.commMessageId, a.timeSent, a.source, a.sender FROM AlertLog a ORDER BY a.timeSent DESC")
    List<Object[]> findDistinctMessages();

    @Query("SELECT DISTINCT a.commMessageId, a.timeSent, a.source, a.sender FROM AlertLog a")
    List<Object[]> findDistinctMessagesOrderBy(Sort sort);

    @Query("SELECT DISTINCT a.commMessageId, a.timeSent, a.source, a.sender FROM AlertLog a")
    Stream<Object[]> streamDistinctMessagesOrderBy(Sort sort);

    @Query("SELECT DISTINCT a.commMessageId, a.timeSent, a.source, a.sender FROM AlertLog a WHERE a.source IN (:sources)")
    Stream<Object[]> streamDistinctMessagesOrderBy(@Param("sources") List<String> sources, Sort sort);

}
