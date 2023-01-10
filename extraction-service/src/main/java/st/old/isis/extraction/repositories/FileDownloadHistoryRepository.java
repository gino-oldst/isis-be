package st.old.isis.extraction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.extraction.entities.FileDownloadHistory;

import java.util.Optional;

public interface FileDownloadHistoryRepository
    extends JpaRepository<FileDownloadHistory, String>,
        JpaSpecificationExecutor<FileDownloadHistory> {

    Optional<FileDownloadHistory> findByFilePath(String filePath);

    Optional<FileDownloadHistory> findByIdAndFilePathAndStatus(String id, String filePath, ProcessedStatus status);

}
