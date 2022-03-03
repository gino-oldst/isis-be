package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.MessageTemplate;

import java.util.Optional;

public interface MessageTemplateRepository
    extends JpaRepository<MessageTemplate, String>, JpaSpecificationExecutor<MessageTemplate> {

  Optional<MessageTemplate> findByName(@NonNull final String name);
}
