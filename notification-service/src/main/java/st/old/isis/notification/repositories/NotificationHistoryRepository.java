package st.old.isis.notification.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.notification.entities.NotificationHistory;

public interface NotificationHistoryRepository
    extends JpaRepository<NotificationHistory, String>,
        JpaSpecificationExecutor<NotificationHistory> {

  Page<NotificationHistory> findByRecipient(@NonNull final String recipient, Pageable pageable);
}
