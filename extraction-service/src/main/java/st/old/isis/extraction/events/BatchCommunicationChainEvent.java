package st.old.isis.extraction.events;

import org.springframework.lang.NonNull;
import st.old.isis.dto.storage.CommunicationChainDto;

public class BatchCommunicationChainEvent extends BatchEvent<CommunicationChainDto> {

  private static final long serialVersionUID = 2542347630348365846L;

  public BatchCommunicationChainEvent(
      @NonNull final Object source, @NonNull final CommunicationChainDto dto) {
    super(source, dto, false, "");
  }

  public BatchCommunicationChainEvent(
      @NonNull final Object source, @NonNull final boolean finalize, String fileSource) {
    super(source, null, finalize, fileSource);
  }
}
