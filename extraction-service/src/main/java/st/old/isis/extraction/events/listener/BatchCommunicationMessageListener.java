package st.old.isis.extraction.events.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.extraction.configurations.BatchConfiguration;
import st.old.isis.extraction.context.DownloadContext;
import st.old.isis.extraction.events.BatchCommunicationMessageEvent;
import st.old.isis.extraction.services.OpenSearchService;
import st.old.isis.extraction.services.internal.AuditService;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.utils.ListUtils;

@Slf4j
@Component
public class BatchCommunicationMessageListener
    extends BatchListEventListener<CommunicationMessageDto, BatchCommunicationMessageEvent> {

  private final StorageService storageService;
  private final OpenSearchService openSearchService;

  public BatchCommunicationMessageListener(
      @NonNull final StorageService storageService,
      @NonNull final NotificationService notificationService,
      @NonNull final BatchConfiguration batchConfiguration,
      @NonNull final AuditService auditService,
      @NonNull final OpenSearchService openSearchService) {
    super(batchConfiguration, notificationService, auditService);
    this.storageService = storageService;
    this.openSearchService= openSearchService;
  }

  @Override
  protected boolean isOnQueue(CommunicationMessageDto obj, CommunicationMessageDto newObj) {
    return false;
  }

  @Override
  protected void persist(ListDto<CommunicationMessageDto> list) throws Throwable {

    if (ListUtils.isNullOrEmpty(list.getData())) {
      return;
    }

    log.debug("Persisting [{}] messages", list.getTotalNoOfRecords());

    try {
//      storageService.saveMessages(list.getData());
        openSearchService.indexBatch(list.getData());
    } catch (Throwable e) {
      // todo: save failed requests to a file for retrying or do an immediate retry
      log.error("Error persisting messages: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  protected void finalizeEvent(String fileSource) {
    DownloadContext.addCompletedFile(fileSource);
  }
}
