package st.old.isis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
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
