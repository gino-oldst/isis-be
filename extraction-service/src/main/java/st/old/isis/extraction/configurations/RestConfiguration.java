package st.old.isis.extraction.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import st.old.isis.extraction.utils.RestUtils;

@Configuration
@RequiredArgsConstructor
public class RestConfiguration {

  private final RestUtils restUtils;

  @Bean
  public OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails() {

    final var resourceDetails = new ClientCredentialsResourceDetails();
    resourceDetails.setAccessTokenUri(restUtils.getAccessTokenUrl());
    resourceDetails.setClientId(restUtils.getClientId());
    resourceDetails.setClientSecret(restUtils.getClientSecret());

    return resourceDetails;
  }

  @Bean
  public OAuth2ClientContext oauth2ClientContext() {
    return new DefaultOAuth2ClientContext();
  }

  @Bean
  @Primary
  @Qualifier("internal")
  public OAuth2RestTemplate oAuth2RestTemplate(
      @NonNull final OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails,
      @NonNull final OAuth2ClientContext oauth2ClientContext) {

    final var restTemplate =
        new OAuth2RestTemplate(oAuth2ProtectedResourceDetails, oauth2ClientContext);

    final var factory = new SimpleClientHttpRequestFactory();

    restTemplate.setRequestFactory(factory);

    return restTemplate;
  }
}
