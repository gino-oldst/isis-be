package st.old.isis.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpointAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomTokenEndpointAuthenticationFilter extends TokenEndpointAuthenticationFilter {

  private static final String GRANT_TYPE = "grant_type";
  private static final String USERNAME = "username";

  private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource =
      new WebAuthenticationDetailsSource();

  public CustomTokenEndpointAuthenticationFilter(
      AuthenticationManager authenticationManager, OAuth2RequestFactory oAuth2RequestFactory) {
    super(authenticationManager, oAuth2RequestFactory);
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    /* before authentication check for condition if true then process to authenticate */
    if ("client_credentials".equals(req.getParameter(GRANT_TYPE))) {
      super.doFilter(req, res, chain);
      return;
    }

    String userName = req.getParameter(USERNAME);

    if (userName == null) {
      super.doFilter(req, res, chain);
      return;
    }

    super.doFilter(req, res, chain);
  }

  @Override
  protected Authentication extractCredentials(HttpServletRequest request) {
    String grantType = request.getParameter(GRANT_TYPE);
    if (grantType != null && grantType.equals("password")) {
      String username = request.getParameter(USERNAME);
      String password = request.getParameter("password");
      UsernamePasswordAuthenticationToken result =
          new UsernamePasswordAuthenticationToken(username, password);
      result.setDetails(this.authenticationDetailsSource.buildDetails(request));
      return result;
    } else {
      return null;
    }
  }

  @Override
  protected void onSuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, Authentication authResult)
      throws IOException {

    super.onSuccessfulAuthentication(request, response, authResult);
  }

  @Override
  protected void onUnsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException {

    super.onUnsuccessfulAuthentication(request, response, failed);
  }
}
