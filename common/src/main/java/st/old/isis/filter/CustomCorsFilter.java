package st.old.isis.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomCorsFilter extends CorsFilter {

  public CustomCorsFilter(CorsConfigurationSource configSource) {
    super(corsConfigurationSource());
  }

  private static CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PATCH", "OPTIONS", "DELETE", "PUT"));
    configuration.setMaxAge(Duration.ofHours(1));
    configuration.setAllowedHeaders(
            Arrays.asList(
                    "x-requested-with",
                    "authorization",
                    "Content-Type",
                    "Authorization",
                    "credential",
                    "X-XSRF-TOKEN",
                    "APIKEY",
                    "SECRETKEY"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
