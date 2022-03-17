package st.old.isis.url;

public abstract class NotificationUrlRepository {

  protected abstract String endpointUrl();

  public NotificationUrl notification() {
    return new NotificationUrl(this);
  }
}
