package st.old.isis.extraction.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Data
@Service
public class DownloadUtils {

  public static final DateTimeFormatter FILENAME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T0000+0000'");

  @Value("${isis.azure.connection-string}")
  private String connectionString;

  @Value("${isis.azure.share-name}")
  private String shareName;

  @Value("${isis.download.timeout}")
  private int timeout;
}
