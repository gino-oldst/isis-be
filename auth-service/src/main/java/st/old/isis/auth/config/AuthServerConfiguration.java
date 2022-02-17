package st.old.isis.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import st.old.isis.auth.utils.JwtUtils;
import st.old.isis.utils.Base64Utils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableAuthorizationServer
public class AuthServerConfiguration extends AuthorizationServerConfigurerAdapter {

  @Autowired private DataSource dataSource;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private UserDetailsService userDetailsService;

  @Autowired private ClientDetailsService clientDetailsService;

  @Autowired private JwtUtils jwtUtils;

  @Autowired
  @Qualifier("authenticationManagerBean")
  private AuthenticationManager authenticationManager;

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(jwtAccessTokenConverter());
  }

  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {

    final var converter = new CustomTokenEnhancer();

    final var keyPair =
        new KeyStoreKeyFactory(
            new Base64Utils(jwtUtils.getJksString()), jwtUtils.getJwtPassword().toCharArray());

    converter.setKeyPair(keyPair.getKeyPair("mariana"));
    return converter;
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

    endpoints
        .tokenGranter(tokenGranter(endpoints))
        .tokenStore(tokenStore())
        .tokenEnhancer(jwtAccessTokenConverter())
        .authenticationManager(authenticationManager)
        .userDetailsService(userDetailsService);
  }

  private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) {
    List<TokenGranter> granters = new ArrayList<>();
    granters.add(new PasswordTokenGranter(endpoints, authenticationManager));
    granters.add(endpoints.getTokenGranter());
    return new CompositeTokenGranter(granters);
  }
}
