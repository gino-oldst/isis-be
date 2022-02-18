package st.old.isis.url;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public abstract class UrlRepository {

  protected abstract String endpointUrl();

  protected abstract String endpoint();

  public UriComponentsBuilder init() {
    return UriComponentsBuilder.fromUri(URI.create(endpointUrl())).pathSegment(endpoint());
  }
}
