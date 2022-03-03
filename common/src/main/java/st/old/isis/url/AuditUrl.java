package st.old.isis.url;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuditUrl extends UrlRepository {

  private final AuditUrlRepository auditUrlRepository;

  @Override
  protected String endpoint() {
    return "audit";
  }

  @Override
  protected String endpointUrl() {
    return auditUrlRepository.endpointUrl();
  }

  public String root() {
    return init().toUriString();
  }
}
