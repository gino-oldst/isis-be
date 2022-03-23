package st.old.isis.extraction.events.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.extraction.configurations.BatchConfiguration;
import st.old.isis.extraction.events.BatchExternalUsersEvent;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.services.internal.StorageService;

@Slf4j
@Component
public class BatchExternalUserListener
    extends BatchListEventListener<ExternalUserDto, BatchExternalUsersEvent> {

  private final StorageService storageService;

  public BatchExternalUserListener(
      @NonNull final StorageService storageService,
      @NonNull final NotificationService notificationService,
      @NonNull final BatchConfiguration batchConfiguration) {
    super(batchConfiguration, notificationService);
    this.storageService = storageService;
  }

  @Override
  protected boolean isOnQueue(ExternalUserDto obj, ExternalUserDto newObj) {
    return obj.getExternalId().equals(newObj.getExternalId());
  }

  @Override
  protected void persist(ListDto<ExternalUserDto> list) throws Throwable {
    log.info("Persisting [{}] external users", list.getTotalNoOfRecords());
    storageService.saveExternalUsers(list.getData());
  }
}
