package st.old.isis.audit.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.audit.services.AuditService;
import st.old.isis.dto.audit.EventLogDto;
import st.old.isis.exception.IsisException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/audit")
public class AuditController {

  private final AuditService service;

  @PostMapping
  public EventLogDto record(@RequestBody final EventLogDto event) throws IsisException {
    return service.record(event);
  }
}
