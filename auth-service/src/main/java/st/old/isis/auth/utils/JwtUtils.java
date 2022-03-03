package st.old.isis.auth.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class JwtUtils {

  @Value("${isis.security.jks}")
  private String jksString;

  @Value("${isis.security.jwt-password}")
  private String jwtPassword;
}
