package st.old.isis.extraction.events;

import st.old.isis.dto.storage.ExternalUserDto;

import java.util.ArrayList;
import java.util.List;

public class BatchExternalUsersEvent extends BatchListEvent<ExternalUserDto> {

  private static final long serialVersionUID = -3804315348376640601L;

  public BatchExternalUsersEvent(Object source, List<ExternalUserDto> users) {
    super(source, users, false);
  }

  public BatchExternalUsersEvent(Object source, boolean finalize) {
    super(source, new ArrayList<>(), finalize);
  }
}
