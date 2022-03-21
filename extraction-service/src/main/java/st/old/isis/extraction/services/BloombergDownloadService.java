package st.old.isis.extraction.services;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.enums.Source;
import st.old.isis.extraction.utils.DownloadUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BloombergDownloadService extends AzureDownloadService {

  //  private final BloombergChatExtractionService extractionService;

  public BloombergDownloadService(
      @NonNull @Qualifier("Bloomberg") final ShareDirectoryClient bloomberg) {
    super(bloomberg);
    //    this.extractionService = extractionService;
  }

  @Override
  @Audited(event = "DOWNLOAD_BLOOMBERG_RAW_DATA")
  public void sync() {
    super.sync();
  }

  @Override
  @Audited(event = "DOWNLOAD_SPECIFIC_BLOOMBERG_RAW_DATA")
  public void sync(@NonNull @Audited final String prefix) {
    super.sync(prefix);
  }

  @Override
  protected String source() {
    return Source.BLOOMBERG.name();
  }

  @Override
  protected String getPrefix(@NonNull final String prefix) {

    final var prefixFormatter = DateTimeFormatter.ofPattern("yy-MM-");

    final var now = LocalDate.now();

    if (now.lengthOfMonth() == now.getDayOfMonth()) {
      return now.format(prefixFormatter);
    }

    final var lastFileDate = LocalDate.parse(prefix, DownloadUtils.BLOOMBERG_FILENAME_FORMAT);

    final var lastDateSuffix = lastFileDate.getDayOfMonth() / 10;
    final var currentDateSuffix = now.getDayOfMonth() / 10;

    if (lastDateSuffix == currentDateSuffix) {
      return lastFileDate.format(prefixFormatter) + currentDateSuffix;
    } else {
      return now.format(prefixFormatter);
    }
  }

  @Override
  protected boolean skipFile(@NonNull final String filename) {
    return filenamePatterns().stream().noneMatch(pattern -> pattern.matcher(filename).find());
  }

  private List<Pattern> filenamePatterns() {
    return List.of(
        Pattern.compile("^f840690.ib19.*\\.xml$"),
        Pattern.compile("^f840690.ib19.att.*\\.tar.gz$"),
        Pattern.compile("^f840690.msg.*\\.xml$"),
        Pattern.compile("^f840690.msg.att.*\\.tar.gz$"));
  }

  @Override
  protected void processFile(@NonNull final ShareFileClient client) {
    // todo: send input stream to parser
  }
}
