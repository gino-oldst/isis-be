package st.old.isis.extraction.services;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.CommunicationMessageDto;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final RestHighLevelClient restHighLevelClient;

    public IndexResponse indexSingleMessage(CommunicationMessageDto message) throws IOException {
        return index("test-index", message);
    }

    public BulkResponse indexBatch(List<CommunicationMessageDto> messages) throws IOException {
        return bulkIndex("test-index", messages);
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
                .withDelay(Duration.ofSeconds(10))
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.error("Connection attempt failed", e.getLastException()))
                .onRetry(e -> log.warn("Failure #{}. Retrying.", e.getAttemptCount()))
                .build();

        return Failsafe.with(retryPolicy).get(() -> restHighLevelClient.bulk(request, RequestOptions.DEFAULT));

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

}
