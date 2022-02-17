package st.old.isis.url;

import org.springframework.lang.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public abstract class StorageUrlRepository {

  protected abstract String endpointUrl();

  public Auth auth() {
    return new Auth();
  }

  public class Auth {

    private final static String endpoint = "auth";

    public String getAppUserInfoAuthenticationUrl(@NonNull final String username) {

      return UriComponentsBuilder.fromUri(URI.create(endpointUrl()))
              .pathSegment(endpoint, username)
              .toUriString();
    }
  }

  //todo: update with url endpoints used internally
}
