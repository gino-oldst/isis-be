package st.old.isis.extraction.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class RestUtils {

  @Value("${isis.internal.authentication.client-id}")
  private String clientId;

  @Value("${isis.internal.authentication.client-secret}")
  private String clientSecret;

  @Value("${isis.internal.authentication.access-token-url}")
  private String accessTokenUrl;
}
