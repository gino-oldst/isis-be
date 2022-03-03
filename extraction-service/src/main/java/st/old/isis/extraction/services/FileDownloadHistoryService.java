package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.extraction.entity.FileDownloadHistory;
import st.old.isis.extraction.repositories.FileDownloadHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadHistoryService {

  private final FileDownloadHistoryRepository repository;

  public void saveHistory(@NonNull final FileDownloadHistory history) {
    repository.save(history);
  }

  public boolean isExisting(@NonNull final String sha1) {
    return repository.findById(sha1).isPresent();
  }
}
