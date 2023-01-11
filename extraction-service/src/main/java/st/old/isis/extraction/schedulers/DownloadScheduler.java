package st.old.isis.extraction.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.amazon.BloombergRawDownloadService;
import st.old.isis.extraction.services.amazon.ReutersRawDownloadService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadScheduler {

  private final BloombergRawDownloadService bloombergRawDownloadService;
  private final ReutersRawDownloadService reutersRawDownloadService;

  @Scheduled(cron = "${isis.download.scheduler.reuters.cron}")
  @SchedulerLock(
      name = "syncYesterdayReutersRawFiles",
      lockAtLeastFor = "${isis.download.scheduler.reuters.lock-time}")
  public void syncYesterdayReutersRawFiles() throws IsisException {
    log.info("Downloading Reuters raw files from AWS S3...");
    reutersRawDownloadService.syncYesterdayDate();
  }

  @Scheduled(cron = "${isis.download.scheduler.bloomberg.cron}")
  @SchedulerLock(
      name = "syncYesterdayBloombergRawFiles",
      lockAtLeastFor = "${isis.download.scheduler.bloomberg.lock-time}")
  public void syncYesterdayBloombergRawFiles() throws IsisException {
    log.info("Downloading Bloomberg raw files from AWS S3...");
    bloombergRawDownloadService.syncYesterdayDate();

  }
}
