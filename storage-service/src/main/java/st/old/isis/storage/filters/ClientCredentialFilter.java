package st.old.isis.storage.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.filter.OncePerRequestFilter;
import st.old.isis.storage.services.ApiKeyAuthorizationService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ClientCredentialFilter extends OncePerRequestFilter {

  private final ApiKeyAuthorizationService apiKeyAuthorizationService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final var apiKey = request.getHeader("APIKEY");
    final var apiSecret = request.getHeader("APISECRET");

    final var notValid = !apiKeyAuthorizationService.validateApiSecretKey(apiKey, apiSecret);

    if (notValid) {
      throw new BadCredentialsException("Invalid API/SECRET KEY");
    }

    filterChain.doFilter(request, response);
  }
}
