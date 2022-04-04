package st.old.isis.auth.config;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import st.old.isis.auth.utils.UserPrincipal;

import java.util.LinkedHashMap;

public class CustomTokenEnhancer extends JwtAccessTokenConverter {

  @Override
  public OAuth2AccessToken enhance(
      OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

    final var customAccessToken = new DefaultOAuth2AccessToken(accessToken);

    if (authentication.getPrincipal() instanceof UserPrincipal) {

      final var userPrincipal = (UserPrincipal) authentication.getPrincipal();
      final var userId = userPrincipal.getUser().getId();
      final var info = new LinkedHashMap<>(accessToken.getAdditionalInformation());
      info.put("uid", userId);
      customAccessToken.setAdditionalInformation(info);
    }

    return super.enhance(customAccessToken, authentication);
  }
}
