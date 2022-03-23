package st.old.isis.extraction.events;

import org.springframework.context.ApplicationEvent;
import st.old.isis.dto.Dto;

public abstract class BatchEvent<T extends Dto> extends ApplicationEvent {

  private static final long serialVersionUID = 3320237610119877193L;

  private final T dto;
  private final boolean finalize;

  public BatchEvent(Object source, T dto, boolean finalize) {
    super(source);
    this.finalize = finalize;
    this.dto = dto;
  }

  public T getDto() {
    return this.dto;
  }

  public boolean shouldFinalize() {
    return this.finalize;
  }
}
