package st.old.isis.url;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationUrl extends UrlRepository {

  private final NotificationUrlRepository notificationUrlRepository;

  @Override
  protected String endpoint() {
    return "notification";
  }

  @Override
  protected String endpointUrl() {
    return notificationUrlRepository.endpointUrl();
  }

  public String root() {
    return init().toUriString();
  }
}
