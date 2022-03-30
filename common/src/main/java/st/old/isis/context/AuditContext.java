package st.old.isis.context;

import org.springframework.lang.NonNull;
import st.old.isis.dto.audit.AuditEvent;
import st.old.isis.dto.audit.EventLogDto;

import java.util.ArrayList;

public class AuditContext {

  private static final ThreadLocal<EventLogDto> threadLocal = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> started = ThreadLocal.withInitial(() -> false);

  public static void init(final String txnId, final String source) {
    threadLocal.set(
        EventLogDto.builder().txnId(txnId).source(source).events(new ArrayList<>()).build());
    started.set(true);
  }

  public static EventLogDto getAuditContext() {
    return threadLocal.get();
  }

  public static boolean hasStarted() {
    return started.get();
  }

  public static void clearAuditContext() {
    threadLocal.remove();
    started.remove();
  }

  public static void addEvent(@NonNull final AuditEvent event) {
    final var eventLog = getAuditContext();
    final var events = eventLog.getEvents();
    final var lastPosition = events.size() - 1;

    if (lastPosition >= 0) {

      final var latestEvent = events.get(lastPosition);

      if (latestEvent.getEvent().equals(event.getEvent())) {
        latestEvent.addOccurrence();
        return;
      }
    }

    events.add(event);
  }
}
