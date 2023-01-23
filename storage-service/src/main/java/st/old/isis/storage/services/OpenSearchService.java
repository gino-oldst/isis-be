package st.old.isis.storage.services;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.search.*;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.OpenSearchMessageDto;
import st.old.isis.storage.configurations.OpenSearchConfiguration;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final RestHighLevelClient restHighLevelClient;

    private final OpenSearchConfiguration configuration;

    public List<OpenSearchMessageDto> searchList(OpenSearchMessageDto dto) throws IOException {
        AtomicInteger counter= new AtomicInteger(0);

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder()
                .must(QueryBuilders.matchQuery("import_date", dto.getImportDate()))
                .must(QueryBuilders.matchQuery("source", dto.getSource()));

        SearchResponse searchResponse;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .size(configuration.getBatchSize());

        SearchRequest firstBatch = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder).scroll(TimeValue.timeValueMinutes(1L));

        searchResponse = restHighLevelClient.search(firstBatch, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();

        // Declare Clear Scroll
        ClearScrollRequest request = new ClearScrollRequest();
        request.addScrollId(scrollId);

        // Process first batch
        long totalNumberOfHits = hits.getHits().length;

        // STEP 2
        // Retrieve all relevant documents/ succeeding batches
        // Loop until results return 0
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

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(IOException.class, ConnectException.class, SocketTimeoutException.class, OpenSearchStatusException.class, SearchPhaseExecutionException.class)
                .withBackoff(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.warn("Connection attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying scroll.", e.getAttemptCount()))
                //TODO Send notification that a batch has failed
                .onFailure(e -> log.error("FAILED - Scrolling through Records in OpenSearch. Max retries reached. {}", e.getException(), e.getException()))
                .build();

        while(hits.getHits().length > 0){
            counter.getAndIncrement();
            log.info("Processing Batch {}", counter.get());

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));

            SearchResponse scrollResponse = Failsafe.with(retryPolicy).get(() -> restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT));

            //  log.info(String.valueOf(scrollResponse.toString()));
            Arrays.stream(scrollResponse.getHits().getHits()).map(s -> new OpenSearchMessageDto(
                    s.getId(),
                    (String) s.getSourceAsMap().get("chain_id"),
                    LocalDateTime.parse(s.getSourceAsMap().get("time_sent").toString()),
                    (String) s.getSourceAsMap().get("sender"),
                    (String) s.getSourceAsMap().get("recipients"),
                    (String) s.getSourceAsMap().get("source"),
                    (String) s.getSourceAsMap().get("type"),
                    (String) s.getSourceAsMap().get("content"),
                    (String) s.getSourceAsMap().get("import_date")
            )).forEachOrdered(result::add);
            //  scrollId = searchScrollResponse.getScrollId();
            hits = scrollResponse.getHits();

            log.info("Added {} of messages", hits.getHits().length);
            log.info("Result size {} of messages ", result.size());

            totalNumberOfHits += hits.getHits().length;

            log.info("Total Number of Hits: {} of {}", totalNumberOfHits, scrollResponse.getHits().getTotalHits().toString());
        }

        log.info("SUCCESS - Processed all relevant messages. Clearing scroll context");
        // Clear scroll context
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-clear-scroll.html
        ClearScrollResponse response = restHighLevelClient.clearScroll(request, RequestOptions.DEFAULT);

        if(response.isSucceeded()){
            log.info("SUCCESS - Cleared scroll context");
            log.info("Cleared Number of Search context: {}", response.getNumFreed());
        } else {
            log.info("FAILED - Clearing scroll context");
        }

        return result;
    }

    public Map<String, Object> initialSearchByDate(LocalDate importDate) throws IOException {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder()
                .must(QueryBuilders.matchQuery("import_date", importDate));

        SearchResponse searchResponse;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .size(configuration.getBatchSize());

        SearchRequest firstBatch = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder).scroll(TimeValue.timeValueMinutes(10L));

        searchResponse = restHighLevelClient.search(firstBatch, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();

        List<CommunicationMessageDto> result = Arrays.stream(searchResponse.getHits().getHits()).map(s -> CommunicationMessageDto.builder()
                .id(s.getId())
                .chainLinkId(s.getSourceAsMap().get("chain_id").toString())
                .timeSent(LocalDateTime.parse(s.getSourceAsMap().get("time_sent").toString()))
                .sender(s.getSourceAsMap().get("sender").toString())
                .recipients(Arrays.asList(s.getSourceAsMap().get("recipients").toString().split("\\s*,\\s*")))
                .source(s.getSourceAsMap().get("source").toString())
                .type(s.getSourceAsMap().get("type").toString())
                .message(s.getSourceAsMap().get("content").toString())
                .importDate(LocalDate.parse(s.getSourceAsMap().get("import_date").toString()))
                .build()
        ).collect(Collectors.toList());

        Map<String, Object> obj = new HashMap<>();
        obj.put("result", result);
        obj.put("scrollId", scrollId);
        obj.put("hits", hits.getHits().length);

        return obj;
    }

    public Map<String, Object> searchScrollByDate(LocalDate importDate, String scrollId, Integer hits) throws IOException {
        AtomicInteger counter= new AtomicInteger(0);

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(IOException.class, ConnectException.class, SocketTimeoutException.class, OpenSearchStatusException.class, SearchPhaseExecutionException.class)
                .withBackoff(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.warn("Connection attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying scroll.", e.getAttemptCount()))
                //TODO Send notification that a batch has failed
                .onFailure(e -> log.error("FAILED - Scrolling through Records in OpenSearch. Max retries reached. {}", e.getException(), e.getException()))
                .build();

        Map<String, Object> obj = new HashMap<>();

        if (hits > 0){
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));

            SearchResponse scrollResponse = Failsafe.with(retryPolicy).get(() -> restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT));

            // log.info(String.valueOf(scrollResponse.toString()));
            List<CommunicationMessageDto> result = Arrays.stream(scrollResponse.getHits().getHits()).map(s -> CommunicationMessageDto.builder()
                    .id(s.getId())
                    .chainLinkId(s.getSourceAsMap().get("chain_id").toString())
                    .timeSent(LocalDateTime.parse(s.getSourceAsMap().get("time_sent").toString()))
                    .sender(s.getSourceAsMap().get("sender").toString())
                    .recipients(Arrays.asList(s.getSourceAsMap().get("recipients").toString().split("\\s*,\\s*")))
                    .source(s.getSourceAsMap().get("source").toString())
                    .type(s.getSourceAsMap().get("type").toString())
                    .message((String) s.getSourceAsMap().get("content"))
                    .importDate(LocalDate.parse(s.getSourceAsMap().get("import_date").toString()))
                    .build()
            ).collect(Collectors.toList());

            hits = scrollResponse.getHits().getHits().length;

            log.info("Added {} of messages", hits);

            obj.put("result", result);
            obj.put("scrollId", scrollId);
            obj.put("hits", hits);
        } else {
            obj.put("result", Collections.emptyList());
            obj.put("scrollId", "");
            obj.put("hits", 0);
        }

        return obj;
    }

    public void clearSearchContext(String scrollId) throws IOException {
        log.info("SUCCESS - Processed all relevant messages. Clearing scroll context");

        // Declare Clear Scroll
        ClearScrollRequest request = new ClearScrollRequest();
        request.addScrollId(scrollId);

        ClearScrollResponse response = restHighLevelClient.clearScroll(request, RequestOptions.DEFAULT);

        if(response.isSucceeded()){
            log.info("SUCCESS - Cleared scroll context");
            log.info("Cleared Number of Search context: {}", response.getNumFreed());
        } else {
            log.info("FAILED - Clearing scroll context");
        }
    }

    public List<OpenSearchMessageDto> search(LocalDate importDate) throws IOException {
        AtomicInteger counter= new AtomicInteger(0);

        // STEP 1
        // Retrieve first batch
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder()
                .must(QueryBuilders.matchQuery("import_date", importDate));

        SearchResponse searchResponse;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .size(configuration.getBatchSize());

        SearchRequest firstBatch = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder).scroll(TimeValue.timeValueMinutes(1L));

        searchResponse = restHighLevelClient.search(firstBatch, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();

        // Declare Clear Scroll
        ClearScrollRequest request = new ClearScrollRequest();
        request.addScrollId(scrollId);

        // Process first batch
        long totalNumberOfHits = hits.getHits().length;

        // STEP 2
        // Retrieve all relevant documents/ succeeding batches
        // Loop until results return 0
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

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(IOException.class, ConnectException.class, SocketTimeoutException.class, OpenSearchStatusException.class, SearchPhaseExecutionException.class)
                .withBackoff(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.warn("Connection attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying scroll.", e.getAttemptCount()))
                //TODO Send notification that a batch has failed
                .onFailure(e -> log.error("FAILED - Scrolling through Records in OpenSearch. Max retries reached. {}", e.getException(), e.getException()))
                .build();

        while(hits.getHits().length > 0){
            counter.getAndIncrement();
            log.info("Processing Batch {}", counter.get());

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));

            SearchResponse scrollResponse = Failsafe.with(retryPolicy).get(() -> restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT));

            //  log.info(String.valueOf(scrollResponse.toString()));
            Arrays.stream(scrollResponse.getHits().getHits()).map(s -> new OpenSearchMessageDto(
                    s.getId(),
                    (String) s.getSourceAsMap().get("chain_id"),
                    LocalDateTime.parse(s.getSourceAsMap().get("time_sent").toString()),
                    (String) s.getSourceAsMap().get("sender"),
                    (String) s.getSourceAsMap().get("recipients"),
                    (String) s.getSourceAsMap().get("source"),
                    (String) s.getSourceAsMap().get("type"),
                    (String) s.getSourceAsMap().get("content"),
                    (String) s.getSourceAsMap().get("import_date")
            )).forEachOrdered(result::add);
            //  scrollId = searchScrollResponse.getScrollId();
            hits = scrollResponse.getHits();

            log.info("Added {} of messages", hits.getHits().length);
            log.info("Result size {} of messages ", result.size());

            totalNumberOfHits += hits.getHits().length;

            log.info("Total Number of Hits: {} of {}", totalNumberOfHits, scrollResponse.getHits().getTotalHits().toString());
        }

        log.info("SUCCESS - Processed all relevant messages. Clearing scroll context");
        // Clear scroll context
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-clear-scroll.html
        ClearScrollResponse response = restHighLevelClient.clearScroll(request, RequestOptions.DEFAULT);

        if(response.isSucceeded()){
            log.info("SUCCESS - Cleared scroll context");
            log.info("Cleared Number of Search context: {}", response.getNumFreed());
        } else {
            log.info("FAILED - Clearing scroll context");
        }

        return result;
    }

    // TODO - Download Filter Via OpenSearch
    @Transactional(timeoutString = "${isis.search.timeout:60}", readOnly = true)
    public List<CommunicationMessageDto> downloadMessages(CommunicationMessageDto filter) throws IOException {
        return filterMessages(filter);
    }

    public List<CommunicationMessageDto> filterMessages(CommunicationMessageDto dto) throws IOException {
        AtomicInteger counter= new AtomicInteger(0);

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder()
//                .must(QueryBuilders.matchQuery("import_date", dto.getImportDate()))
//                .must(QueryBuilders.matchQuery("source", dto.getSource()))
                .filter(QueryBuilders.rangeQuery("time_sent")
                        .gte(dto.getDateSentFrom())
                        .lte(dto.getDateSentTo())
                        .format("yyyy-MM-dd")
                        .relation("within"))
                .filter(QueryBuilders.matchPhraseQuery("type", "Send"))
                .mustNot(QueryBuilders.matchPhraseQuery("content", "[Bloomberg created note: Remains as a participant of the room.]"));

        if (!dto.getSenders().isEmpty()){
            dto.getSenders().forEach(obj -> {
                queryBuilder.should(
                    QueryBuilders.matchPhraseQuery("sender", obj)
                );
            });
        } else if (!dto.getRecipients().isEmpty()) {
            dto.getRecipients().forEach(obj -> {
                queryBuilder.should(
                        QueryBuilders.matchPhraseQuery("recipients", obj)
                );
            });
        } else if (!dto.getSources().isEmpty()) {
            dto.getSources().forEach(obj -> {
                queryBuilder.should(
                        QueryBuilders.matchPhraseQuery("source", obj)
                );
            });
        }


        queryBuilder.minimumShouldMatch(queryBuilder.should().size() -1);
        //sure sure ang filter dateSentFrom, dateSentTo
        // senders - array ni siya
        // recipients - array ni siya
        // sources - array ni siya
        // keyword - str

        SearchResponse searchResponse;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .size(configuration.getBatchSize());

        SearchRequest firstBatch = Requests.searchRequest(configuration.getIndexPath())
                .source(searchSourceBuilder).scroll(TimeValue.timeValueMinutes(1L));

        searchResponse = restHighLevelClient.search(firstBatch, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();

        // Declare Clear Scroll
        ClearScrollRequest request = new ClearScrollRequest();
        request.addScrollId(scrollId);

        // Process first batch
        long totalNumberOfHits = hits.getHits().length;

        // STEP 2
        // Retrieve all relevant documents/ succeeding batches
        // Loop until results return
        List<CommunicationMessageDto> result = Arrays.stream(searchResponse.getHits().getHits()).map(s -> CommunicationMessageDto.builder()
                .id(s.getId())
                .chainLinkId(s.getSourceAsMap().get("chain_id").toString())
                .timeSent(LocalDateTime.parse(s.getSourceAsMap().get("time_sent").toString()))
                .sender(s.getSourceAsMap().get("sender").toString())
                .recipients(Arrays.asList(s.getSourceAsMap().get("recipients").toString().split("\\s*,\\s*")))
                .source(s.getSourceAsMap().get("source").toString())
                .type(s.getSourceAsMap().get("type").toString())
                .message(s.getSourceAsMap().get("content").toString())
                .importDate(LocalDate.parse(s.getSourceAsMap().get("import_date").toString()))
                .build()
        ).collect(Collectors.toList());

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(IOException.class, ConnectException.class, SocketTimeoutException.class, OpenSearchStatusException.class, SearchPhaseExecutionException.class)
                .withBackoff(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.warn("Connection attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying scroll.", e.getAttemptCount()))
                //TODO Send notification that a batch has failed
                .onFailure(e -> log.error("FAILED - Scrolling through Records in OpenSearch. Max retries reached. {}", e.getException(), e.getException()))
                .build();

        while(hits.getHits().length > 0){
            counter.getAndIncrement();
            log.info("Processing Batch {}", counter.get());

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));

            SearchResponse scrollResponse = Failsafe.with(retryPolicy).get(() -> restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT));

            //  log.info(String.valueOf(scrollResponse.toString()));
            Arrays.stream(scrollResponse.getHits().getHits()).map(s -> CommunicationMessageDto.builder()
                    .id(s.getId())
                    .chainLinkId(s.getSourceAsMap().get("chain_id").toString())
                    .timeSent(LocalDateTime.parse(s.getSourceAsMap().get("time_sent").toString()))
                    .sender(s.getSourceAsMap().get("sender").toString())
                    .recipients(Arrays.asList(s.getSourceAsMap().get("recipients").toString().split("\\s*,\\s*")))
                    .source(s.getSourceAsMap().get("source").toString())
                    .type(s.getSourceAsMap().get("type").toString())
                    .message(s.getSourceAsMap().get("content").toString())
                    .importDate(LocalDate.parse(s.getSourceAsMap().get("import_date").toString()))
                    .build()
            ).forEachOrdered(result::add);
            //  scrollId = searchScrollResponse.getScrollId();
            hits = scrollResponse.getHits();

            log.info("Added {} of messages", hits.getHits().length);
            log.info("Result size {} of messages ", result.size());

            totalNumberOfHits += hits.getHits().length;

            log.info("Total Number of Hits: {} of {}", totalNumberOfHits, scrollResponse.getHits().getTotalHits().toString());
        }

        log.info("SUCCESS - Processed all relevant messages. Clearing scroll context");
        // Clear scroll context
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-clear-scroll.html
        ClearScrollResponse response = restHighLevelClient.clearScroll(request, RequestOptions.DEFAULT);

        if(response.isSucceeded()){
            log.info("SUCCESS - Cleared scroll context");
            log.info("Cleared Number of Search context: {}", response.getNumFreed());
        } else {
            log.info("FAILED - Clearing scroll context");
        }

        return result;
    }
}
