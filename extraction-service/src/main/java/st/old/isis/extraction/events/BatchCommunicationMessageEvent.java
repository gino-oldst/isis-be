package st.old.isis.extraction.events;

import st.old.isis.dto.storage.CommunicationMessageDto;

import java.util.ArrayList;
import java.util.List;

public class BatchCommunicationMessageEvent extends BatchListEvent<CommunicationMessageDto> {

  private static final long serialVersionUID = -2249708530685876479L;

  public BatchCommunicationMessageEvent(Object source, List<CommunicationMessageDto> users) {
    super(source, users, false);
  }

  public BatchCommunicationMessageEvent(Object source, boolean finalize) {
    super(source, new ArrayList<>(), finalize);
  }
}
