package st.old.isis.extraction.events.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.extraction.configurations.BatchConfiguration;
import st.old.isis.extraction.context.DownloadContext;
import st.old.isis.extraction.events.BatchCommunicationRecipientEvent;
import st.old.isis.extraction.services.internal.AuditService;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.utils.ListUtils;

@Slf4j
@Component
public class BatchCommunicationRecipientListener
    extends BatchListEventListener<CommunicationRecipientDto, BatchCommunicationRecipientEvent> {

  private final StorageService storageService;

  public BatchCommunicationRecipientListener(
      @NonNull final StorageService storageService,
      @NonNull final NotificationService notificationService,
      @NonNull final BatchConfiguration batchConfiguration,
      @NonNull final AuditService auditService) {
    super(batchConfiguration, notificationService, auditService);
    this.storageService = storageService;
  }

  @Override
  protected boolean isOnQueue(CommunicationRecipientDto obj, CommunicationRecipientDto newObj) {
    return false;
  }

  @Override
  protected void persist(ListDto<CommunicationRecipientDto> list) throws Throwable {

    if (ListUtils.isNullOrEmpty(list.getData())) {
      return;
    }

    log.debug("Persisting [{}] recipients", list.getTotalNoOfRecords());

    try {
      storageService.saveRecipients(list.getData());
    } catch (Throwable e) {
      // todo: save failed requests to a file for retrying or do an immediate retry
      log.error("Error persisting recipients: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  protected void finalizeEvent(String fileSource) {
    DownloadContext.addCompletedFile(fileSource);
  }
}
