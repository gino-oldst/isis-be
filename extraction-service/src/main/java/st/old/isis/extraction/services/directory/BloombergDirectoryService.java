package st.old.isis.extraction.services.directory;

import com.azure.storage.file.share.ShareDirectoryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.enums.Source;

@Slf4j
@Service
public class BloombergDirectoryService extends AzureDirectoryService {

  public BloombergDirectoryService(
      @NonNull @Qualifier("Bloomberg") final ShareDirectoryClient reuters) {
    super(reuters);
  }

  @Override
  protected String source() {
    return Source.BLOOMBERG.name();
  }
}
