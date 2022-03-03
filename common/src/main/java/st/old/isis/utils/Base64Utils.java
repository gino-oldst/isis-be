package st.old.isis.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
public class Base64Utils extends AbstractFileResolvingResource {

  private final String base64String;

  public Base64Utils(@NonNull final String base64String) {
    this.base64String = base64String;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public InputStream getInputStream() throws IOException {

    log.debug("Converting base64string to input stream");

    return new ByteArrayInputStream(Base64.getDecoder().decode(base64String));
  }
}
