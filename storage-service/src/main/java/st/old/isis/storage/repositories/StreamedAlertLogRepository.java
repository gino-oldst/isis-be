package st.old.isis.storage.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.AlertLog;

import java.util.stream.Stream;

public interface StreamedAlertLogRepository {

    Stream<AlertLog> findAll(@NonNull Specification<AlertLog> spec);

}
