package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.enums.Source;
import st.old.isis.extraction.entity.DownloadVersionControl;
import st.old.isis.extraction.repositories.DownloadVersionControlRepository;
import st.old.isis.extraction.utils.DownloadUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadVersionControlService {

  private final DownloadVersionControlRepository repository;

  @Audited(event = "RECORD_LAST_FILE_DOWNLOADED")
  public void saveVersionControl(@NonNull @Audited final DownloadVersionControl version) {

    final var source = version.getSource();

    final var optVersion = getVersionControl(source);

    if (optVersion.isPresent()) {
      final var v = optVersion.get();

      final DateTimeFormatter filenameFormat;

      switch (Source.valueOf(source)) {
        case REUTERS:
          filenameFormat = DownloadUtils.REUTERS_FILENAME_FORMAT;
          break;
        case BLOOMBERG:
          filenameFormat = DownloadUtils.BLOOMBERG_FILENAME_FORMAT;
          break;
        default:
          filenameFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      }

      final var newDate =
          LocalDateTime.of(
              LocalDate.parse(version.getLastFileName(), filenameFormat), LocalTime.MIN);

      optVersion
          .map(DownloadVersionControl::getLastFileName)
          .map(date -> LocalDate.parse(date, filenameFormat))
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
