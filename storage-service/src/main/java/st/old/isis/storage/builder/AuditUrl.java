package st.old.isis.storage.builder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import st.old.isis.url.AuditUrlRepository;

@Service
public class AuditUrl extends AuditUrlRepository {

  @Value("${isis.internal.services.audit.endpoint}")
  private String endpoint;

  @Override
  protected String endpointUrl() {
    return endpoint;
  }
}
