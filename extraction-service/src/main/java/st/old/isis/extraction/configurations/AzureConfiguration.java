package st.old.isis.extraction.configurations;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareClientBuilder;
import com.azure.storage.file.share.ShareDirectoryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import st.old.isis.extraction.utils.DownloadUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AzureConfiguration {

  private final DownloadUtils downloadUtils;

  @Bean
  @Qualifier("ShareClient")
  public ShareClient shareClient() {
    return new ShareClientBuilder()
        .connectionString(downloadUtils.getConnectionString())
        .shareName(downloadUtils.getShareName())
        .buildClient();
  }

  @Bean
  @Qualifier("RootDirectory")
  public ShareDirectoryClient recordingsRootDirectory(
      @NonNull @Qualifier("ShareClient") final ShareClient shareClient) {
    return shareClient.getRootDirectoryClient();
  }

  @Bean
  @Qualifier("Bloomberg")
  public ShareDirectoryClient bloombergDirectory(
      @NonNull @Qualifier("RootDirectory") final ShareDirectoryClient rootDirectory) {
    return rootDirectory.getSubdirectoryClient(downloadUtils.getBloombergDirectory());
  }

  @Bean
  @Qualifier("IceChat")
  public ShareDirectoryClient iceChatDirectory(
      @NonNull @Qualifier("RootDirectory") final ShareDirectoryClient rootDirectory) {
    return rootDirectory.getSubdirectoryClient(downloadUtils.getIceChatDirectory());
  }

  @Bean
  @Qualifier("Reuters")
  public ShareDirectoryClient reutersDirectory(
      @NonNull @Qualifier("RootDirectory") final ShareDirectoryClient rootDirectory) {
    return rootDirectory.getSubdirectoryClient(downloadUtils.getReutersDirectory());
  }

  @Bean
  @Qualifier("Symphony")
  public ShareDirectoryClient symphonyDirectory(
      @NonNull @Qualifier("RootDirectory") final ShareDirectoryClient rootDirectory) {
    return rootDirectory.getSubdirectoryClient(downloadUtils.getSymphonyDirectory());
  }

  @Bean
  @Qualifier("Whatsapp")
  public ShareDirectoryClient whatsappDirectory(
      @NonNull @Qualifier("RootDirectory") final ShareDirectoryClient rootDirectory) {
    return rootDirectory.getSubdirectoryClient(downloadUtils.getWhatsappDirectory());
  }
}
