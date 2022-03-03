package st.old.isis.context;

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
}
