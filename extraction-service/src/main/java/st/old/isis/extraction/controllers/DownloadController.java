package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
  public void downloadBloomberg() {
    bloombergDownloadService.sync();
  }

  @GetMapping("/bloomberg/search")
  public void downloadBloomberg(@RequestParam @NonNull final String prefix) {
    bloombergDownloadService.sync(prefix);
  }

  @GetMapping("/icechat")
  public void downloadIceChat() {
    iceChatDownloadService.sync();
  }

  @GetMapping("/reuters")
  public void downloadReuters() {
    reutersDownloadService.sync();
  }

  @GetMapping("/reuters/search")
  public void downloadReuters(@RequestParam @NonNull final String prefix) {
    reutersDownloadService.sync(prefix);
  }

  @GetMapping("/symphony")
  public void downloadSymphony() {
    symphonyDownloadService.sync();
  }

  @GetMapping("/whatsapp")
  public void downloadWhatsapp() {
    whatsappDownloadService.sync();
  }
}
