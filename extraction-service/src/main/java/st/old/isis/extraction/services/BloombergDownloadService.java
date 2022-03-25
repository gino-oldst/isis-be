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

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BloombergDownloadService extends AzureDownloadService {

  private final Pattern CHAT_FILE_PATTERN = Pattern.compile("^f840690.ib19.*\\.xml$");
  private final Pattern EMAIL_FILE_PATTERN = Pattern.compile("^f840690.msg.*\\.xml$");

  private final BloombergChatExtractionService chatExtractionService;
  private final BloombergEmailExtractionService emailExtractionService;

  public BloombergDownloadService(
      @NonNull @Qualifier("Bloomberg") final ShareDirectoryClient bloomberg,
      @NonNull final BloombergChatExtractionService chatExtractionService,
      @NonNull final BloombergEmailExtractionService emailExtractionService) {
    super(bloomberg);
    this.chatExtractionService = chatExtractionService;
    this.emailExtractionService = emailExtractionService;
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
    return List.of(CHAT_FILE_PATTERN, EMAIL_FILE_PATTERN);
  }

  @Override
  protected void processFile(
      @NonNull final ShareFileClient client, @NonNull final String filename) {
    try {

      final var inputStream = client.openInputStream();
      final var now = LocalDate.now();

      if (isChat(filename)) {
        log.info("Chat");
        chatExtractionService.parse(inputStream, now);
      }

      if (isEmail(filename)) {
        log.info("Email");
        emailExtractionService.parse(inputStream, now);
      }

    } catch (JAXBException | XMLStreamException e) {
      log.error("Error processing xml file: {}", e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean isChat(@NonNull final String filename) {
    return CHAT_FILE_PATTERN.matcher(filename).find();
  }

  private boolean isEmail(@NonNull final String filename) {
    return EMAIL_FILE_PATTERN.matcher(filename).find();
  }
}
