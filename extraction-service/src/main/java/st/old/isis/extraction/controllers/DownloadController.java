package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.annotations.Audited;
import st.old.isis.extraction.services.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/download")
public class DownloadController {

  private final BloombergDownloadService bloombergDownloadService;
  private final IceChatDownloadService iceChatDownloadService;
  private final ReutersDownloadService reutersDownloadService;
  private final SymphonyDownloadService symphonyDownloadService;
  private final WhatsappDownloadService whatsappDownloadService;

  @GetMapping("/bloomberg")
  @Audited(event = "MANUAL_SYNC_BLOOMBERG")
  public void downloadBloomberg() {
    bloombergDownloadService.sync();
  }

  @GetMapping("/bloomberg/search")
  @Audited(event = "MANUAL_SPECIFIC_SYNC_BLOOMBERG")
  public void downloadBloomberg(@Audited @RequestParam @NonNull final String prefix) {
    bloombergDownloadService.sync(prefix);
  }

  @GetMapping("/icechat")
  @Audited(event = "MANUAL_SYNC_ICE_CHAT")
  public void downloadIceChat() {
    iceChatDownloadService.sync();
  }

  @GetMapping("/reuters")
  @Audited(event = "MANUAL_SYNC_REUTERS")
  public void downloadReuters() {
    reutersDownloadService.sync();
  }

  @GetMapping("/reuters/search")
  @Audited(event = "MANUAL_SPECIFIC_SYNC_REUTERS")
  public void downloadReuters(@RequestParam @NonNull final String prefix) {
    reutersDownloadService.sync(prefix);
  }

  @GetMapping("/symphony")
  @Audited(event = "MANUAL_SYNC_SYMPHONY")
  public void downloadSymphony() {
    symphonyDownloadService.sync();
  }

  @GetMapping("/whatsapp")
  @Audited(event = "MANUAL_SYNC_WHATSAPP")
  public void downloadWhatsapp() {
    whatsappDownloadService.sync();
  }
}
