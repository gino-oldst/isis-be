package st.old.isis.url;

public abstract class AuditUrlRepository {

  protected abstract String endpointUrl();

  public AuditUrl auditUrl() {
    return new AuditUrl(this);
  }
}
