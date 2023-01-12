package st.old.isis.extraction.configurations;

import lombok.Data;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import st.old.isis.extraction.constants.AppConstants;

@Configuration
@Getter
public class OpenSearchConfiguration {

    @Value(AppConstants.AWS_OPENSEARCH_USERNAME)
    private String username;

    @Value(AppConstants.AWS_OPENSEARCH_PASSWORD)
    private String password;

    @Value(AppConstants.AWS_OPENSEARCH_HOSTNAME)
    private String hostname;

    @Value(AppConstants.AWS_OPENSEARCH_INDEX_PATH)
    private String indexPath;

    @Bean
    public RestHighLevelClient restHighLevelClient(){

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        return new RestHighLevelClient(builder);

    }

}
