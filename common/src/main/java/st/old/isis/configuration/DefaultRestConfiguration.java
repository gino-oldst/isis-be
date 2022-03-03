package st.old.isis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DefaultRestConfiguration {

  @Bean
  public RestTemplate defaultRestTemplate() {
    return new RestTemplate();
  }
}
