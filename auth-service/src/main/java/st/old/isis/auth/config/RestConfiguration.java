package st.old.isis.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;
import st.old.isis.auth.utils.RestUtils;

@Configuration
public class RestConfiguration {

  @Bean
  @Qualifier("internal")
  public RestTemplate restTemplate(@NonNull final RestUtils restUtils) {
    return new RestTemplateBuilder(
            rt ->
                rt.getInterceptors()
                    .add(
                        (request, body, execution) -> {
                          request.getHeaders().add("APIKEY", restUtils.getApiKey());
                          request.getHeaders().add("APISECRET", restUtils.getApiSecret());
                          return execution.execute(request, body);
                        }))
        .build();
  }
}
