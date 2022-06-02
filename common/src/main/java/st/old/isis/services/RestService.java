package st.old.isis.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import st.old.isis.exception.IsisException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestService {

  private final RestTemplate restTemplate;

  public <T> T get(@NonNull String uri, @NonNull HttpEntity<?> entity, Class<T> returnedClass) {

    log.debug("Sending GET request to {} ", uri);

    final var response = restTemplate.exchange(uri, HttpMethod.GET, entity, returnedClass);
    return response.getBody();
  }

  public <T> T get(@NonNull String uri, Class<T> returnedClass) throws IsisException {

    log.debug("Sending GET request to {} ", uri);

    try {

      final var response = restTemplate.getForEntity(uri, returnedClass);
      return response.getBody();

    } catch (Exception e) {
      throw IsisException.internalServerError(e.getMessage(), "RestClientException");
    }
  }

  public <T> T post(
      @NonNull String uri,
      @Nullable HttpEntity<?> entity,
      ParameterizedTypeReference<T> responseType) {

    log.debug("Sending POST request to {} ", uri);

    final var response = restTemplate.exchange(uri, HttpMethod.POST, entity, responseType);
    return response.getBody();
  }

  public <T> T post(@NonNull String uri, @Nullable HttpEntity<?> entity, Class<T> returnedClass) {

    log.debug("Sending POST request to {} ", uri);

    final var response = restTemplate.exchange(uri, HttpMethod.POST, entity, returnedClass);
    return response.getBody();
  }

  public <T> T put(@NonNull String uri, @Nullable HttpEntity<?> entity, Class<T> returnedClass) {

    log.debug("Sending PUT request to {} ", uri);

    final var response = restTemplate.exchange(uri, HttpMethod.PUT, entity, returnedClass);
    return response.getBody();
  }
}
