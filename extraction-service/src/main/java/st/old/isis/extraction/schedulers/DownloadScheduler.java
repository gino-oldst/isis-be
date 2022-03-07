package st.old.isis.extraction.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import st.old.isis.extraction.services.AzureDownloadService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadScheduler {

  private final List<AzureDownloadService> service;

  @Scheduled(cron = "${isis.download.scheduler.cron}")
  @SchedulerLock(name = "downloadRawFiles",
          lockAtLeastFor = "${isis.download.scheduler.lock-time}")
  public void downloadRawFiles() {

    log.info("Downloading raw files from azure...");

    service.forEach(AzureDownloadService::sync);
  }
}
