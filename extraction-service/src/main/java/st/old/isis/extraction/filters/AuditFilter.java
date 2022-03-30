package st.old.isis.extraction.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import st.old.isis.context.AuditContext;
import st.old.isis.dto.audit.AuditEvent;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.internal.AuditService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

  private final AuditService auditService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    log.info("Start auditing...");

    if (isAsyncDispatch(request)) {
      filterChain.doFilter(request, response);
    } else {
      doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
    }
  }

  protected void doFilterWrapped(
      ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response,
      FilterChain filterChain)
      throws ServletException, IOException {
    try {
      beforeRequest(request, response);
      filterChain.doFilter(request, response);
    } finally {
      afterRequest(request, response);
      response.copyBodyToResponse();
    }
  }

  protected void beforeRequest(
      ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {

    log.info("Auditing request...");

    final var txnHeader = request.getHeader("txn-id");
    final var txnId = Strings.isNotEmpty(txnHeader) ? txnHeader : UUID.randomUUID().toString();

    AuditContext.init(txnId, "Extraction Service");
  }

  protected void afterRequest(
      ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {

    log.info("Auditing response...");

    try {

      final var status = response.getStatus();
      final var success = status >= 200 && status < 300;

      final var audit = AuditContext.getAuditContext();

      final AuditEvent event;

      if (success) {
        event = AuditEvent.builder().event("REQUEST_SUCCESS").build();
      } else {
        event = AuditEvent.builder().event("REQUEST_FAILED").build();
      }

      audit.getEvents().add(event);

      try {
        auditService.recordEvent(audit);
      } catch (IsisException e) {
        log.error(String.format("Error sending audited events: %s", audit), e);
      }

    } finally {
      AuditContext.clearAuditContext();
    }
  }

  private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
    if (request instanceof ContentCachingRequestWrapper) {
      return (ContentCachingRequestWrapper) request;
    } else {
      return new ContentCachingRequestWrapper(request);
    }
  }

  private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
    if (response instanceof ContentCachingResponseWrapper) {
      return (ContentCachingResponseWrapper) response;
    } else {
      return new ContentCachingResponseWrapper(response);
    }
  }
}
