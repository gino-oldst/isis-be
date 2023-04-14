package st.old.isis.extraction.services;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.TermQueryBuilder;
import org.opensearch.index.reindex.BulkByScrollResponse;
import org.opensearch.index.reindex.DeleteByQueryRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.extraction.configurations.OpenSearchConfiguration;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final OpenSearchConfiguration configuration;

    public IndexResponse indexSingleMessage(CommunicationMessageDto message) throws IOException {
        return index(configuration.getIndexPath(), message);
    }

    public BulkResponse indexBatch(List<CommunicationMessageDto> messages) throws IOException {
        return bulkIndex(configuration.getIndexPath(), messages);
    }

    public BulkByScrollResponse purgeProcess() {

        final LocalDate today = LocalDate.now();
        final var purgeDate = today.minusMonths(configuration.getPurgePeriodMonths());

        log.info("Purging OpenSearch started for Import Date {}", purgeDate);

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(IOException.class, ConnectException.class, SocketTimeoutException.class, OpenSearchStatusException.class)
                .withBackoff(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.warn("Purging attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying purge process.", e.getAttemptCount()))
                //TODO Send notification that a batch has failed
                .onFailure(e -> log.error("FAILED - Indexing Batch to OpenSearch. Max retries reached. {}", e.getException()))
                .build();

        final var response =  Failsafe.with(retryPolicy).get(() -> deleteByQuery(purgeDate));

        log.info("Purging OpenSearch complete for Import Date {}", purgeDate);

        return response;
    }

    private IndexResponse index(String indexPath, CommunicationMessageDto message) throws IOException {
        IndexRequest request = new IndexRequest(indexPath); //Add a document to the custom-index we created.
        //        request.id("2"); //Assign an ID to the document.
        request.source(parseCommMessage(message)); //Place your content into the index's source.
        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    private BulkResponse bulkIndex(String indexPath, List<CommunicationMessageDto> messages) throws IOException {
        BulkRequest request = new BulkRequest();

        for(CommunicationMessageDto message: messages){
            final var indexRequest= new IndexRequest(indexPath);
            indexRequest.source(parseCommMessage(message));
            request.add(indexRequest);
        }

        log.info("Ingesting {} messages into OpenSearch. Estimated number of bytes at {}", request.requests().size(), request.estimatedSizeInBytes());

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(IOException.class, ConnectException.class, SocketTimeoutException.class, OpenSearchStatusException.class)
                .withBackoff(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.warn("Connection attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying bulk index.", e.getAttemptCount()))
                //TODO Send notification that a batch has failed
                .onFailure(e -> log.error("FAILED - Indexing Batch to OpenSearch. Max retries reached. {}", e.getException()))
                .build();

        return Failsafe.with(retryPolicy).get(() -> restHighLevelClient.bulk(request, RequestOptions.DEFAULT));

    }
    public BulkByScrollResponse deleteByQuery(@NonNull LocalDate importDate) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(configuration.getIndexPath());
        request.setQuery(new TermQueryBuilder("import_date", importDate.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        System.out.println(request);
        request.setRefresh(true);
        return restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
    }

    private Map<String, String> parseCommMessage(CommunicationMessageDto message){

        HashMap<String, String> stringMapping = new HashMap();
        stringMapping.put("chain_id", message.getChainLinkId());
        stringMapping.put("time_sent", message.getTimeSent().toString());
        stringMapping.put("sender", message.getSender());
        stringMapping.put("recipients", String.join(",", message.getRecipients()));
        stringMapping.put("source", message.getSource());
        stringMapping.put("type", message.getType());
        stringMapping.put("content", message.getMessage());
        stringMapping.put("import_date", message.getImportDate().toString());

        return stringMapping;
    }

    @Async
    public BulkByScrollResponse purgeByDate(@NonNull LocalDate dataDate){
        log.info("Purging OpenSearch started for Historic Data with Import Date {}", dataDate);

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(IOException.class, ConnectException.class, SocketTimeoutException.class, OpenSearchStatusException.class)
                .withBackoff(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.warn("Purging attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying purge process.", e.getAttemptCount()))
                //TODO Send notification that a batch has failed
                .onFailure(e -> log.error("FAILED - Indexing Batch to OpenSearch. Max retries reached. {}", e.getException()))
                .build();

        final var response =  Failsafe.with(retryPolicy).get(() -> deleteByQuery(dataDate));

        log.info("Purging OpenSearch complete for Historic Data with Import Date {}", dataDate);

        return response;
    }

}
