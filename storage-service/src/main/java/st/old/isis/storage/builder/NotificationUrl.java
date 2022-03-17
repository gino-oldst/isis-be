package st.old.isis.storage.builder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import st.old.isis.url.NotificationUrlRepository;

@Service
public class NotificationUrl extends NotificationUrlRepository {

  @Value("${isis.internal.services.notification.endpoint}")
  private String endpoint;

  @Override
  protected String endpointUrl() {
    return endpoint;
  }
}
