package st.old.isis.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class PasswordTokenGranter extends AbstractTokenGranter {

  private static final String GRANT_TYPE = "password";
  private static final String PASSWORD_PARAMETER = "password";
  private static final GrantedAuthority PRE_AUTH = new SimpleGrantedAuthority("PRE_AUTH");

  private final AuthenticationManager authenticationManager;

  public PasswordTokenGranter(
      AuthorizationServerEndpointsConfigurer endpointsConfigurer,
      AuthenticationManager authenticationManager) {
    super(
        endpointsConfigurer.getTokenServices(),
        endpointsConfigurer.getClientDetailsService(),
        endpointsConfigurer.getOAuth2RequestFactory(),
        GRANT_TYPE);
    this.authenticationManager = authenticationManager;
  }

  @Override
  protected OAuth2Authentication getOAuth2Authentication(
      ClientDetails client, TokenRequest tokenRequest) {
    Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
    String username = parameters.get("username");
    String password = parameters.get(PASSWORD_PARAMETER);
    parameters.remove(PASSWORD_PARAMETER);
    Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
    ((AbstractAuthenticationToken) userAuth).setDetails(parameters);

    try {
      userAuth = this.authenticationManager.authenticate(userAuth);
    } catch (AccountStatusException | BadCredentialsException e) {
      throw new InvalidGrantException(e.getMessage());
    }
    OAuth2Authentication oAuth2Authentication;
    if (userAuth != null && userAuth.isAuthenticated()) {
      OAuth2Request storedOAuth2Request =
          this.getRequestFactory().createOAuth2Request(client, tokenRequest);
      oAuth2Authentication = new OAuth2Authentication(storedOAuth2Request, userAuth);

      return oAuth2Authentication;
    } else {
      throw new InvalidGrantException("Could not authenticate user: " + username);
    }
  }
}
