package st.old.isis.storage.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.AlertLog;

import java.util.List;
import java.util.stream.Stream;

public interface StreamedAlertLogRepository {

    Stream<AlertLog> findAll(@NonNull Specification<AlertLog> spec);

}
