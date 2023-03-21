package st.old.isis.extraction.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.OpenSearchService;
import st.old.isis.extraction.services.amazon.BloombergRawDownloadService;
import st.old.isis.extraction.services.amazon.ReutersRawDownloadService;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurgeScheduler {
  private final OpenSearchService openSearchService;

  @Scheduled(cron = "${isis.purge.scheduler.cron}")
  @SchedulerLock(
      name = "purgeProcess",
      lockAtLeastFor = "${isis.purge.scheduler.lock-time}")
  public void purgeProcess() {
    log.info("Purging Process initiated ...");
    openSearchService.purgeProcess();
    log.info("Purging Process Completed!");
  }

}
