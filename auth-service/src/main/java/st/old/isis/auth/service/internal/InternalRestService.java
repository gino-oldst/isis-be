package st.old.isis.auth.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import st.old.isis.services.RestService;

@Slf4j
@Service
public class InternalRestService extends RestService {

  public InternalRestService(@NonNull @Qualifier("internal") final RestTemplate restTemplate) {
    super(restTemplate);
  }
}
