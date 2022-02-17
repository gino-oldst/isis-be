package st.old.isis.extraction.configurations;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("isis-admin")
                .pathsToMatch("/**")
                .build();
    }

}
