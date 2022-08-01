package st.old.isis.extraction.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import st.old.isis.enums.Source;

import java.time.format.DateTimeFormatter;

@Data
@Service
public class DownloadUtils {

  public static final DateTimeFormatter BLOOMBERG_FILENAME_FORMAT =
      DateTimeFormatter.ofPattern("yyMMdd");

  public static final DateTimeFormatter REUTERS_FILENAME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T0000+0000'");

  @Value("${isis.azure.connection-string}")
  private String connectionString;

  @Value("${isis.azure.share-name}")
  private String shareName;

  @Value("${isis.download.timeout}")
  private int timeout;

  @Value("${isis.download.directory.bloomberg}")
  private String bloombergDirectory;

  @Value("${isis.download.directory.ice-chat}")
  private String iceChatDirectory;

  @Value("${isis.download.directory.reuters}")
  private String reutersDirectory;

  @Value("${isis.download.directory.symphony}")
  private String symphonyDirectory;

  @Value("${isis.download.directory.whatsapp}")
  private String whatsappDirectory;

  public String determineDirectoryFromSource(String source) {

    final var sourceEnum = Source.valueOf(source);

    switch (sourceEnum) {
      case BLOOMBERG:
        return bloombergDirectory;
      case ICE_CHAT:
        return iceChatDirectory;
      case REUTERS:
        return reutersDirectory;
      case SYMPHONY:
        return symphonyDirectory;
      case WHATSAPP:
        return whatsappDirectory;
    }
    return null;

  }
}
