package st.old.isis.url;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExternalUserUrl extends UrlRepository {

  private final StorageUrlRepository storageUrlRepository;

  @Override
  protected String endpoint() {
    return "external";
  }

  @Override
  protected String endpointUrl() {
    return storageUrlRepository.endpointUrl();
  }

  public String root() {
    return init().toUriString();
  }

  public String batch() {
    return init().pathSegment("batch").toUriString();
  }

  public String merge() {
    return init().pathSegment("merge").toUriString();
  }
}
