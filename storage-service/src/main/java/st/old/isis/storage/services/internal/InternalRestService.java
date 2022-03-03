package st.old.isis.storage.services.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import st.old.isis.services.RestService;

@Slf4j
@Service
public class InternalRestService extends RestService {

  public InternalRestService(
      @NonNull @Qualifier("internal") final OAuth2RestTemplate restTemplate) {
    super(restTemplate);
  }
}
