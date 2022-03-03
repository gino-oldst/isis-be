package st.old.isis.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpEntityBuilder {

  private final HttpHeaders headers;
  private Object body;

  private HttpEntityBuilder() {
    this.headers = new HttpHeaders();
  }

  public static HttpEntityBuilder builder() {
    return new HttpEntityBuilder();
  }

  public HttpEntityBuilder addHeader(String key, String value) {
    this.headers.add(key, value);
    return this;
  }

  public HttpEntityBuilder jsonContent() {
    headers.setContentType(MediaType.APPLICATION_JSON);
    return this;
  }

  public HttpEntityBuilder contentType(MediaType mediaType) {
    headers.setContentType(mediaType);
    return this;
  }

  public HttpEntityBuilder body(Object body) {
    this.body = body;
    return this;
  }

  public HttpEntity<Object> build() {
    return (this.body != null)
        ? new HttpEntity<>(this.body, this.headers)
        : new HttpEntity<>(headers);
  }
}
