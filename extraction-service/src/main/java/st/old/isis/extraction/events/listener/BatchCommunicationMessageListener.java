package st.old.isis.extraction.events.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.extraction.configurations.BatchConfiguration;
import st.old.isis.extraction.events.BatchCommunicationMessageEvent;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.utils.ObjectUtils;

@Slf4j
@Component
public class BatchCommunicationMessageListener
    extends BatchListEventListener<CommunicationMessageDto, BatchCommunicationMessageEvent> {

  private final StorageService storageService;
  private final ObjectUtils objectUtils;

  public BatchCommunicationMessageListener(
      @NonNull final StorageService storageService,
      @NonNull final ObjectUtils objectUtils,
      @NonNull final NotificationService notificationService,
      @NonNull final BatchConfiguration batchConfiguration) {
    super(batchConfiguration, notificationService);
    this.storageService = storageService;
    this.objectUtils = objectUtils;
  }

  @Override
  protected boolean isOnQueue(CommunicationMessageDto obj, CommunicationMessageDto newObj) {
    return false;
  }

  @Override
  protected void persist(ListDto<CommunicationMessageDto> list) throws Throwable {
    log.info("Persisting [{}] messages", list.getTotalNoOfRecords());
    try {
      storageService.saveMessages(list.getData());
    } catch (Throwable e) {
      //todo: save failed requests to a file for retrying or do an immediate retry
      log.error("Error persisting messages: {}", objectUtils.toString(list.getData()));
      throw e;
    }
  }
}
