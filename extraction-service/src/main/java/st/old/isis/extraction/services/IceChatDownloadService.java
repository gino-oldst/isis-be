package st.old.isis.extraction.services;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IceChatDownloadService extends AzureDownloadService {

  // todo: ICE Chat extraction service

  public IceChatDownloadService(@NonNull @Qualifier("IceChat") final ShareDirectoryClient iceChat) {
    super(iceChat);
  }

  @Override
  protected String source() {
    return "ICE Chat";
  }

  @Override
  protected boolean skipFile(@NonNull final String filename) {
    // todo: implement skip file logic based on filename
    return false;
  }

  @Override
  protected void processFile(@NonNull final ShareFileClient client) {
    // todo: implement ICE Chat file processing
  }
}
