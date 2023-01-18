package st.old.isis.url;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataImportRecordUrl extends UrlRepository {

  private final StorageUrlRepository storageUrlRepository;

  @Override
  protected String endpoint() {
    return "data-import-record";
  }

  @Override
  protected String endpointUrl() {
    return storageUrlRepository.endpointUrl();
  }

  public String root() {
    return init().toUriString();
  }

}
