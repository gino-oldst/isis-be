package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.*;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import st.old.isis.storage.configurations.OpenSearchConfiguration;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final RestHighLevelClient restHighLevelClient;

    private final OpenSearchConfiguration configuration;

    public void search() throws IOException {
//        String query = "{\"term\":{\"import_date\":{\"query\":\"2023-01-12\"}}}";
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder()
                .must(QueryBuilders.matchQuery("import_date", "2023-01-12"));

        SearchResponse searchResponse;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder).from(0).size(10);

        SearchRequest searchRequest = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder);

        searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //Getting back the document
//        GetRequest getRequest = new GetRequest(configuration.getIndexPath(), "1");
//        GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        log.info(String.valueOf(searchResponse.toString()));
    }

}
