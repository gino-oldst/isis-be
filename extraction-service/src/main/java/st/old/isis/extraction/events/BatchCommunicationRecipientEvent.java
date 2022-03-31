package st.old.isis.extraction.events;

import st.old.isis.dto.storage.CommunicationRecipientDto;

import java.util.ArrayList;
import java.util.List;

public class BatchCommunicationRecipientEvent extends BatchListEvent<CommunicationRecipientDto> {

  private static final long serialVersionUID = -7303233405883575413L;

  public BatchCommunicationRecipientEvent(Object source, List<CommunicationRecipientDto> users) {
    super(source, users, false);
  }

  public BatchCommunicationRecipientEvent(Object source, boolean finalize) {
    super(source, new ArrayList<>(), finalize);
  }
}
