package st.old.isis.extraction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import st.old.isis.extraction.entity.DownloadVersionControl;

import java.util.Optional;

public interface DownloadVersionControlRepository
    extends JpaRepository<DownloadVersionControl, String>,
        JpaSpecificationExecutor<DownloadVersionControl> {

  Optional<DownloadVersionControl> findBySource(@NonNull final String source);
}
