package st.old.isis.storage.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.CommunicationMessage;

import java.util.stream.Stream;

public interface StreamedCommunicationMessageRepository {

  Stream<CommunicationMessage> findAll(@NonNull Specification<CommunicationMessage> spec);
}
