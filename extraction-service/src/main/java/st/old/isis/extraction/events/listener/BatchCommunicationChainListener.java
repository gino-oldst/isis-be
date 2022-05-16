package st.old.isis.extraction.events.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.dto.storage.TempDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.configurations.BatchConfiguration;
import st.old.isis.extraction.events.BatchCommunicationChainEvent;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.services.internal.StorageService;
import st.old.isis.utils.ListUtils;
import st.old.isis.utils.ObjectUtils;

@Slf4j
@Component
public class BatchCommunicationChainListener
    extends BatchEventListener<CommunicationChainDto, BatchCommunicationChainEvent> {

  private final StorageService storageService;
  private final ObjectUtils objectUtils;

  public BatchCommunicationChainListener(
      @NonNull final StorageService storageService,
      @NonNull final ObjectUtils objectUtils,
      @NonNull final NotificationService notificationService,
      @NonNull final BatchConfiguration batchConfiguration) {
    super(batchConfiguration, notificationService);
    this.storageService = storageService;
    this.objectUtils = objectUtils;
  }

  @Override
  protected boolean isOnQueue(CommunicationChainDto obj, CommunicationChainDto newObj) {
    return false; // return false no need a filter for this object
  }

  @Override
  protected void persist(ListDto<CommunicationChainDto> list) throws Throwable {

    if (ListUtils.isNullOrEmpty(list.getData())) {
      return;
    }

    log.info("Importing [{}] communication chains", list.getTotalNoOfRecords());

    try {
      storageService.importChain(list);
    } catch (Throwable e) {
      // todo: save failed requests to a file for retrying or do an immediate retry
      log.error("Error persisting communication chains: {}", objectUtils.toString(list));
      throw e;
    }
  }

  @Override
  protected void finalizeEvent(String fileSource) throws IsisException {
    storageService.mergeChain(TempDto.builder().fileSource(fileSource).build());
  }
}
