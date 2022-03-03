package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.extraction.entity.DownloadVersionControl;
import st.old.isis.extraction.repositories.DownloadVersionControlRepository;
import st.old.isis.extraction.utils.DownloadUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadVersionControlService {

  private final DownloadVersionControlRepository repository;

  @Audited(event = "RECORD_LAST_FILE_DOWNLOADED")
  public void saveVersionControl(@NonNull @Audited final DownloadVersionControl version) {

    final var optVersion = getVersionControl(version.getSource());

    if (optVersion.isPresent()) {
      final var v = optVersion.get();

      final var newDate =
          LocalDateTime.of(
              LocalDate.parse(version.getLastFileName(), DownloadUtils.FILENAME_FORMAT),
              LocalTime.MIN);

      optVersion
          .map(DownloadVersionControl::getLastFileName)
          .map(date -> LocalDate.parse(date, DownloadUtils.FILENAME_FORMAT))
          .map(date -> LocalDateTime.of(date, LocalTime.MIN))
          .ifPresent(
              date -> {
                if (date.isBefore(newDate)) {
                  v.setLastFileName(version.getLastFileName());
                  repository.save(v);
                }
              });

    } else {
      repository.save(version);
    }
  }

  @Audited(event = "GET_LAST_DOWNLOADED_FILE")
  public Optional<DownloadVersionControl> getVersionControl(@NonNull @Audited final String source) {
    return repository.findBySource(source);
  }
}
