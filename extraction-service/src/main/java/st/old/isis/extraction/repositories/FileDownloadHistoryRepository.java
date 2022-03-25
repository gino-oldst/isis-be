package st.old.isis.extraction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.extraction.entities.FileDownloadHistory;

public interface FileDownloadHistoryRepository
    extends JpaRepository<FileDownloadHistory, String>,
        JpaSpecificationExecutor<FileDownloadHistory> {}
