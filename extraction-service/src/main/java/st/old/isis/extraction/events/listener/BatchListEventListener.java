package st.old.isis.extraction.events.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import st.old.isis.dto.Dto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.configurations.BatchConfiguration;
import st.old.isis.extraction.events.BatchListEvent;
import st.old.isis.extraction.services.internal.AuditService;
import st.old.isis.extraction.services.internal.NotificationService;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
public abstract class BatchListEventListener<T extends Dto, B extends BatchListEvent<T>>
    implements ApplicationListener<B> {

  private final BatchConfiguration batchConfiguration;
  private final NotificationService notificationService;
  private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
  private final AuditService auditService;

  protected abstract boolean isOnQueue(T obj, T newObj);

  protected abstract void persist(ListDto<T> list) throws Throwable;

  protected abstract void finalizeEvent(String fileSource) throws IsisException;

  @Override
  public void onApplicationEvent(@NonNull B event) {

    try {

      if (event.shouldFinalize()) {
        log.debug("Finalize batch persist [{}] remaining objects", queue.size());
        if (!queue.isEmpty()) {
          processBatch(queue.size());
        }
        finalizeEvent(event.getFileSource());
        return;
      }

      event
          .getList()
          .forEach(
              dto -> {
                final var notExisting = queue.stream().noneMatch(obj -> isOnQueue(obj, dto));

                if (notExisting) {
                  queue.offer(dto);
                }
              });

      if (queue.size() >= batchConfiguration.getBatchSize()) {
        log.debug(
            "Batch persist [{} of {}] objects", batchConfiguration.getBatchSize(), queue.size());

        final var cycle = queue.size() / batchConfiguration.getBatchSize();

        for (int x = 0; x < cycle; x++) {
          processBatch(batchConfiguration.getBatchSize());
        }
      }

    } catch (Throwable err) {
      auditService.recordEvent(
          "Extraction Service",
          "BATCH_PERSISTING_ERROR",
          Map.of(
              "batchEvent", event.getClass().getSimpleName(), "filename", event.getFileSource()));
      sendBatchPersistingError(err);
    }
  }

  private void processBatch(final int size) throws Throwable {
    final var batch = new ArrayList<T>();

    for (int x = 0; x < size; x++) {
      Optional.ofNullable(queue.poll()).ifPresent(batch::add);
    }

    final var list =
        ListDto.<T>builder()
            .data(batch)
            .totalPages(1)
            .totalNoOfRecords(batch.size())
            .totalCurrentElements(batch.size())
            .build();

    persist(list);
  }

  private void sendBatchPersistingError(Throwable e) {
    log.error("An error occurred when persisting batched objects", e);

    final var request =
        NotificationRequestDto.builder()
            .customMessage(
                MessageDto.builder()
                    .subject("Persisting Failed")
                    .body(String.format("Persisting batched objects failed: %s", e.getMessage()))
                    .build())
            // .recipients() todo: recipients
            .build();

    //    notificationService.sendNotification(List.of(request));
  }
}
