package st.old.isis.url;

public abstract class StorageUrlRepository {

  protected abstract String endpointUrl();

  public AuthUrl auth() {
    return new AuthUrl(this);
  }

  public CommunicationUrl communication() {
    return new CommunicationUrl(this);
  }

  public ChainUrl chain() {
    return new ChainUrl(this);
  }

  public ExternalUserUrl externalUser() {
    return new ExternalUserUrl(this);
  }

  public MessageUrl message() {
    return new MessageUrl(this);
  }

  public RecipientUrl recipient() {
    return new RecipientUrl(this);
  }

  public MergeUrl merge() {
    return new MergeUrl(this);
  }
}
