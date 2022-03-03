package st.old.isis.extraction.services;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;

@Slf4j
@Service
public class SymphonyDownloadService extends AzureDownloadService {

  // todo: Symphony extraction service

  public SymphonyDownloadService(
      @NonNull @Qualifier("Symphony") final ShareDirectoryClient symphony) {
    super(symphony);
  }

  @Override
  @Audited(event = "DOWNLOAD_SYMPHONY_RAW_DATA")
  public void sync() {
    super.sync();
  }

  @Override
  @Audited(event = "DOWNLOAD_SPECIFIC_SYMPHONY_RAW_DATA")
  public void sync(@NonNull @Audited final String prefix) {
    super.sync(prefix);
  }

  @Override
  protected String source() {
    return "Symphony";
  }

  @Override
  protected boolean skipFile(@NonNull final String filename) {
    // todo: implement skip file logic based on filename
    return false;
  }

  @Override
  protected void processFile(@NonNull final ShareFileClient client) {
    // todo: implement symphony file processing
  }
}
