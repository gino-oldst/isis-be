package st.old.isis.extraction.events;

import org.springframework.context.ApplicationEvent;
import st.old.isis.dto.Dto;

import java.util.List;

public abstract class BatchListEvent<T extends Dto> extends ApplicationEvent {

  private static final long serialVersionUID = -5776455510394591045L;

  private final List<T> dto;
  private final boolean finalize;

  public BatchListEvent(Object source, List<T> dto, boolean finalize) {
    super(source);
    this.finalize = finalize;
    this.dto = dto;
  }

  public List<T> getList() {
    return this.dto;
  }

  public boolean shouldFinalize() {
    return this.finalize;
  }
}
