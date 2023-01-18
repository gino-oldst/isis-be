package st.old.isis.extraction.builder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import st.old.isis.url.StorageUrlRepository;

@Service
public class DataImportRecordUrl extends StorageUrlRepository {

  @Value("${isis.internal.services.storage.endpoint}")
  private String endpoint;

  @Override
  protected String endpointUrl() {
    return endpoint;
  }
}
