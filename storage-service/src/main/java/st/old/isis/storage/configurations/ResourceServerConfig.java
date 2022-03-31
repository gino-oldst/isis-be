package st.old.isis.storage.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableResourceServer
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

  @Value("${isis.security.jwt-public-key}")
  private String publicKey;

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {

    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setVerifierKey(publicKey);

    return converter;
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Bean
  @Primary
  public DefaultTokenServices tokenServices(@NonNull final TokenStore tokenStore) {
    DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
    defaultTokenServices.setTokenStore(tokenStore);
    defaultTokenServices.setSupportRefreshToken(true);
    return defaultTokenServices;
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer config) {
    config.tokenServices(tokenServices(tokenStore()));
    config.resourceId("INTERNAL_RESOURCE");
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/actuator/**")
        .permitAll()
        .antMatchers("/auth/**")
        .permitAll()
        .antMatchers(endpoints())
        .access("#oauth2.hasScope('INTERNAL')")
        .antMatchers(adminEndpoints())
        .hasAnyAuthority("ADMIN")
        .anyRequest()
        .authenticated()
        .and()
        .cors()
        .and()
        .csrf()
        .disable()
        .httpBasic()
        .disable()
        .exceptionHandling()
        .authenticationEntryPoint(
            (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .accessDeniedHandler(
            (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED));
  }

  private String[] endpoints() {
    return List.of(
            "/external/**",
            "/alert/**",
            "/alert-policy/**",
            "/app-user/**",
            "/communication/**",
            "/keyword/**",
            "/notification/**",
            "/policy-map/**",
            "/user/**",
            "/user-link/**")
        .toArray(new String[0]);
  }

  private String[] adminEndpoints() {
    return List.of(
                    "/external/**",
                    "/alert/**",
                    "/alert-policy/**",
                    "/app-user/**",
                    "/communication/**",
                    "/keyword/**",
                    "/notification/**",
                    "/policy-map/**",
                    "/user/**",
                    "/user-link/**")
            .toArray(new String[0]);
  }
}
