package st.old.isis.extraction.events.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.dto.storage.TempDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.configurations.BatchConfiguration;
import st.old.isis.extraction.events.BatchExternalUsersEvent;
import st.old.isis.extraction.services.internal.AuditService;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.utils.ListUtils;
import st.old.isis.utils.ObjectUtils;

@Slf4j
@Component
public class BatchExternalUserListener
    extends BatchListEventListener<ExternalUserDto, BatchExternalUsersEvent> {

  private final StorageService storageService;
  private final ObjectUtils objectUtils;

  public BatchExternalUserListener(
      @NonNull final StorageService storageService,
      @NonNull final ObjectUtils objectUtils,
      @NonNull final NotificationService notificationService,
      @NonNull final BatchConfiguration batchConfiguration,
      @NonNull final AuditService auditService) {
    super(batchConfiguration, notificationService, auditService);
    this.storageService = storageService;
    this.objectUtils = objectUtils;
  }

  @Override
  protected boolean isOnQueue(ExternalUserDto obj, ExternalUserDto newObj) {
    return obj.getExternalId().equals(newObj.getExternalId());
  }

  @Override
  protected void persist(ListDto<ExternalUserDto> list) throws Throwable {

    if (ListUtils.isNullOrEmpty(list.getData())) {
      return;
    }

    log.info("Persisting [{}] external users", list.getTotalNoOfRecords());

    try {
      storageService.saveExternalUsers(list.getData());
    } catch (Throwable e) {
      // todo: save failed requests to a file for retrying or do an immediate retry
      log.error("Error persisting external users: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  protected void finalizeEvent(String fileSource) throws IsisException {
    storageService.mergeExternalUsers(TempDto.builder().fileSource(fileSource).build());
  }
}
