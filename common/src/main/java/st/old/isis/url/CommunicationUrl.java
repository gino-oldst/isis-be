package st.old.isis.url;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommunicationUrl extends UrlRepository {

  private final StorageUrlRepository storageUrlRepository;

  @Override
  protected String endpoint() {
    return "communication";
  }

  @Override
  protected String endpointUrl() {
    return storageUrlRepository.endpointUrl();
  }

  public String root() {
    return init().toUriString();
  }

  public String search() {
    return init().pathSegment("search").toUriString();
  }
}
