package st.old.isis.url;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class AuthUrl extends UrlRepository {

  private final StorageUrlRepository storageUrlRepository;

  @Override
  protected String endpoint() {
    return "auth";
  }

  @Override
  protected String endpointUrl() {
    return storageUrlRepository.endpointUrl();
  }

  public String getAppUserInfoAuthenticationUrl(@NonNull final String username) {
    return init().pathSegment(username).toUriString();
  }
}
