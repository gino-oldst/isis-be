package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.enums.BatchResultType;
import st.old.isis.storage.services.MergeService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/merge")
public class MergeController {

  private final MergeService mergeService;

  @PostMapping(path = "/batch")
  public void getMessages(@RequestBody Map<BatchResultType, List<String>> files) {
    mergeService.merge(files);
  }
}
