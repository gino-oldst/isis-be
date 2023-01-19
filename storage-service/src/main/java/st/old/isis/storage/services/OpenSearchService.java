package st.old.isis.storage.services;

import com.fasterxml.jackson.core.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.OpenSearchMessageDto;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.configurations.OpenSearchConfiguration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import st.old.isis.storage.entities.PolicyMap;

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
                .query(queryBuilder).from(0).size(10); //TODO - Must get everything no size

        SearchRequest searchRequest = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder).scroll(new TimeValue(60000));

        searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<OpenSearchMessageDto> result = Arrays.stream(searchResponse.getHits().getHits()).map(s -> new OpenSearchMessageDto(
            s.getId(),
            (String) s.getSourceAsMap().get("chain_id"),
            (String) s.getSourceAsMap().get("time_sent"),
            (String) s.getSourceAsMap().get("sender"),
            (String) s.getSourceAsMap().get("recipients"),
            (String) s.getSourceAsMap().get("source"),
            (String) s.getSourceAsMap().get("type"),
            (String) s.getSourceAsMap().get("content"),
            (String) s.getSourceAsMap().get("import_date")
        )).collect(Collectors.toList());
        log.info(String.valueOf(searchResponse.toString()));
        log.info(String.valueOf(result));
        log.info(Arrays.toString(searchResponse.getHits().getHits()));

        // after first request
//        scrollResp = restHighLevelClient.searchScroll(new SearchScrollRequest(searchResponse.getScrollId()).scroll(new TimeValue(60000)));
//        scrollResp = restHighLevelClient.s
    }
}
