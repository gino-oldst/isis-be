package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.Keyword;

import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, String>,
        JpaSpecificationExecutor<Keyword> {

    Optional<Keyword> findByWord(@NonNull final String word);
}
