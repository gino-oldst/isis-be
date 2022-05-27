package st.old.isis.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.context.AuditContext;
import st.old.isis.dto.audit.AuditEvent;
import st.old.isis.exception.IsisException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditUtil {

  private final ObjectUtils objectUtils;

  public void audit(@NonNull final String event) {
    audit(event, "", new HashMap<>());
  }

  public void audit(@NonNull final String event, @NonNull final String description) {
    audit(event, description, new HashMap<>());
  }

  public void audit(@NonNull final String event, @NonNull final Map<String, Object> metadata) {
    audit(event, "", metadata);
  }

  public AuditEvent auditEvent(
      @NonNull final String event, @NonNull final Map<String, Object> metadata) {

    final var auditEvent = AuditEvent.builder().event(event).build();

    if (!metadata.isEmpty()) {
      try {
        auditEvent.setMetadata(objectUtils.toString(metadata));
      } catch (IsisException e) {
        log.error("Error converting metadata to string: {}", e.getMessage());
        e.printStackTrace();
      }
    }

    return auditEvent;
  }

  public void audit(
      @NonNull final String event,
      @NonNull final String description,
      @NonNull final Map<String, Object> metadata) {

    if (!AuditContext.hasStarted()) {
      return;
    }

    final var auditContext = AuditContext.getAuditContext();

    final var auditEvent = AuditEvent.builder().event(event).description(description).build();

    if (!metadata.isEmpty()) {
      try {
        auditEvent.setMetadata(objectUtils.toString(metadata));
      } catch (IsisException e) {
        log.error("Error converting metadata to string: {}", e.getMessage());
        e.printStackTrace();
      }
    }

    auditContext.getEvents().add(auditEvent);
  }
}
