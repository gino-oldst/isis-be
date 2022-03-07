package st.old.isis.url;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChainUrl extends UrlRepository {

  private final StorageUrlRepository storageUrlRepository;

  @Override
  protected String endpoint() {
    return "chain";
  }

  @Override
  protected String endpointUrl() {
    return storageUrlRepository.endpointUrl();
  }

  public String root() {
    return init().toUriString();
  }

  public String importChain() {
    return init().pathSegment("import").toUriString();
  }
}
