package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.exception.IsisException;
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

  public void completeHistory(@NonNull @Audited final String id) throws IsisException {
    updateHistory(id, ProcessedStatus.COMPLETED);
  }

  public void failHistory(@NonNull @Audited final String id) throws IsisException {
    updateHistory(id, ProcessedStatus.FAILED);
  }

  @Audited(event = "UPDATE_DOWNLOADED_FILES")
  public void updateHistory(@NonNull @Audited final String id, @NonNull @Audited final ProcessedStatus status) throws IsisException {

    log.info("Updating download history id {} to status {}", id, status.name());

    final var existingRecord = repository.findById(id);

    if(!existingRecord.isPresent()){
      throw IsisException.notFound("File download history not found ", "NotFoundException");
    }

    final var updatedRecord = existingRecord.get();
    updatedRecord.setStatus(status);

    repository.save(updatedRecord);
  }

  @Audited(event = "CHECK_DOWNLOADED_FILES_ID")
  public boolean isExisting(@NonNull @Audited final String id) {
    return repository.findById(id).isPresent();
  }

  @Audited(event = "CHECK_DOWNLOADED_FILES_ID_FILEPATH")
  public boolean isExistingByIdAndKeyAndStatus(@NonNull @Audited final String id, @NonNull @Audited final String key, final ProcessedStatus status) {
    return repository.findByIdAndFilePathAndStatus(id, key, status).isPresent();
  }
}
