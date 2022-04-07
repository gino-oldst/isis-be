package st.old.isis.extraction.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import st.old.isis.extraction.services.BloombergDownloadService;
import st.old.isis.extraction.services.ReutersDownloadService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadScheduler {

  private final ReutersDownloadService reutersDownloadService;
  private final BloombergDownloadService bloombergDownloadService;

  @Scheduled(cron = "${isis.download.scheduler.reuters.cron}")
  @SchedulerLock(
      name = "downloadReutersRawFiles",
      lockAtLeastFor = "${isis.download.scheduler.reuters.lock-time}")
  public void downloadReutersRawFiles() {

    log.info("Downloading Reuters raw files from azure...");

    reutersDownloadService.sync();
  }

  @Scheduled(cron = "${isis.download.scheduler.bloomberg.cron}")
  @SchedulerLock(
          name = "downloadBloombergRawFiles",
          lockAtLeastFor = "${isis.download.scheduler.bloomberg.lock-time}")
  public void downloadBloombergRawFiles() {

    log.info("Downloading Bloomberg raw files from azure...");

    bloombergDownloadService.sync();
  }
}
