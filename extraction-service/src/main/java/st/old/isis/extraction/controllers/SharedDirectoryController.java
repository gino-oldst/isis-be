package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.annotations.Audited;
import st.old.isis.extraction.services.directory.BloombergDirectoryService;
import st.old.isis.extraction.services.directory.ReutersDirectoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/directory")
public class SharedDirectoryController {

  private final BloombergDirectoryService bloombergDirectoryService;
  private final ReutersDirectoryService reutersDirectoryService;

  @DeleteMapping("/bloomberg")
  @Audited(event = "DELETE_BLOOMBERG_FILE")
  public void deleteBloombergFile(
      @Audited @RequestParam final String directory,
      @Audited @RequestParam final String filename) {
    bloombergDirectoryService.deleteDirectory(directory, filename);
  }

  @DeleteMapping("/reuters")
  @Audited(event = "DELETE_REUTERS_FILE")
  public void deleteReutersFile(
      @Audited @RequestParam final String directory,
      @Audited @RequestParam final String filename) {
    reutersDirectoryService.deleteDirectory(directory, filename);
  }
}
