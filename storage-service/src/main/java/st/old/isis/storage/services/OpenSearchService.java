package st.old.isis.storage.services;

import com.fasterxml.jackson.core.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.*;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.OpenSearchMessageDto;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.configurations.OpenSearchConfiguration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public List<OpenSearchMessageDto> search(LocalDate importDate) throws IOException {
//        String query = "{\"term\":{\"import_date\":{\"query\":\"2023-01-12\"}}}";
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder()
                .must(QueryBuilders.matchQuery("import_date", importDate));

        SearchResponse searchResponse;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder).from(0).size(1000); //TODO - Must get everything no size

        SearchRequest searchRequest = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder).scroll(new TimeValue(60000));

        searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<OpenSearchMessageDto> result = Arrays.stream(searchResponse.getHits().getHits()).map(s -> new OpenSearchMessageDto(
            s.getId(),
            (String) s.getSourceAsMap().get("chain_id"),
            LocalDateTime.parse(s.getSourceAsMap().get("time_sent").toString()),
            (String) s.getSourceAsMap().get("sender"),
            (String) s.getSourceAsMap().get("recipients"),
            (String) s.getSourceAsMap().get("source"),
            (String) s.getSourceAsMap().get("type"),
            (String) s.getSourceAsMap().get("content"),
            (String) s.getSourceAsMap().get("import_date")
        )).collect(Collectors.toList());
//        log.info(String.valueOf(searchResponse.toString()));
//        log.info(String.valueOf(result));
//        log.info(Arrays.toString(searchResponse.getHits().getHits()));

        return result;
        // after first request
//        scrollResp = restHighLevelClient.searchScroll(new SearchScrollRequest(searchResponse.getScrollId()).scroll(new TimeValue(60000)));
//        scrollResp = restHighLevelClient.s

    }

    public void scroll() throws IOException {
//        String query = "{\"term\":{\"import_date\":{\"query\":\"2023-01-12\"}}}";

        AtomicInteger counter= new AtomicInteger(0);
        // STEP 1
        // Retrieve first batch
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder()
                .must(QueryBuilders.matchQuery("import_date", "2023-01-12"));

        SearchResponse searchResponse;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .size(100);

        SearchRequest firstBatch = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder).scroll(TimeValue.timeValueMinutes(1L));

        searchResponse = restHighLevelClient.search(firstBatch, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();

        // Process first batch
        log.info(String.valueOf(searchResponse.toString()));

        long totalNumberOfHits = hits.getHits().length;
        // STEP 2
        // Retrieve all relevant documents/ succeeding batches
        // Loop until results return 0
        while(hits.getHits().length > 0){
            counter.getAndIncrement();
            log.info("Processing Batch {}", counter.get());

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
            SearchResponse scrollResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);

            log.info(String.valueOf(scrollResponse.toString()));
//            scrollId = searchScrollResponse.getScrollId();
            hits = scrollResponse.getHits();

            log.info("Added {} of messages ", hits.getHits().length);

            totalNumberOfHits += hits.getHits().length;

            log.info("Total Number of Hits: {}", totalNumberOfHits);
        }

        log.info("SUCCESS - Processed all relevant messages. Clearing scroll context");
        // Clear scroll context
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-clear-scroll.html
        log.info("SUCCESS - Cleared scroll context");

    }

}
