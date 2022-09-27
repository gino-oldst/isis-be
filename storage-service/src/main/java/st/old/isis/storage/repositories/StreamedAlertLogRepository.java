package st.old.isis.storage.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.AlertLog;

import javax.persistence.Tuple;
import java.util.stream.Stream;

public interface StreamedAlertLogRepository {

    Stream<AlertLog> findAll(@NonNull Specification<AlertLog> spec);

    Stream<String> findDistinctMessageIds(@NonNull Specification<AlertLog> spec);

    Page<Tuple> findDistinctMessageIdsPaged(@NonNull Specification<AlertLog> spec, Pageable page);

}
