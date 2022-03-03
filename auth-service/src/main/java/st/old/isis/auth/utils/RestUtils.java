package st.old.isis.auth.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class RestUtils {

  @Value("${isis.internal.authentication.api-key}")
  private String apiKey;

  @Value("${isis.internal.authentication.api-secret}")
  private String apiSecret;
}
