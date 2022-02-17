package st.old.isis.auth.config;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

public class CustomTokenEnhancer extends JwtAccessTokenConverter {

  @Override
  public OAuth2AccessToken enhance(
      OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

    final var customAccessToken = new DefaultOAuth2AccessToken(accessToken);

    return super.enhance(customAccessToken, authentication);
  }
}
