package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.extraction.entities.FileDownloadHistory;
import st.old.isis.extraction.repositories.FileDownloadHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadHistoryService {

  private final FileDownloadHistoryRepository repository;

  @Audited(event = "RECORD_DOWNLOADED_FILES")
  public void saveHistory(@NonNull @Audited final FileDownloadHistory history) {
    repository.save(history);
  }

  @Audited(event = "CHECK_DOWNLOADED_FILES_ID")
  public boolean isExisting(@NonNull @Audited final String id) {
    return repository.findById(id).isPresent();
  }
}
