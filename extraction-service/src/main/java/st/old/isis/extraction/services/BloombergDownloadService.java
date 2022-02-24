package st.old.isis.extraction.services;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BloombergDownloadService extends AzureDownloadService {

  // todo: Bloomberg extraction service

  public BloombergDownloadService(
      @NonNull @Qualifier("Bloomberg") final ShareDirectoryClient bloomberg) {
    super(bloomberg);
  }

  @Override
  protected String source() {
    return "Bloomberg";
  }

  @Override
  protected boolean skipFile(@NonNull final String filename) {
    // todo: implement skip file logic based on filename
    return false;
  }

  @Override
  protected void processFile(@NonNull final ShareFileClient client) {
    // todo: implement bloomberg file processing
  }
}
