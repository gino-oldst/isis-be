package st.old.isis.storage.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import st.old.isis.storage.filters.ClientCredentialFilter;
import st.old.isis.storage.services.ApiKeyAuthorizationService;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FilterConfiguration {

  private final ApiKeyAuthorizationService apiKeyAuthorizationService;

  @Bean
  public FilterRegistrationBean<ClientCredentialFilter> clientCredentialFilterRegistrationBean() {
    FilterRegistrationBean<ClientCredentialFilter> registrationBean =
        new FilterRegistrationBean<>();
    registrationBean.setFilter(new ClientCredentialFilter(apiKeyAuthorizationService));
    registrationBean.addUrlPatterns(urlPatternsForClientCredentials());
    return registrationBean;
  }

  private String[] urlPatternsForClientCredentials() {
    return List.of("/auth/*").toArray(new String[0]);
  }
}
