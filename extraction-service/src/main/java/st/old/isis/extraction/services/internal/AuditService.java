package st.old.isis.extraction.services.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import st.old.isis.configuration.AuditConfiguration;
import st.old.isis.dto.audit.EventLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.builder.AuditUrl;
import st.old.isis.utils.AuditUtil;
import st.old.isis.utils.HttpEntityBuilder;
import st.old.isis.utils.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditUrl auditUrl;
  private final InternalRestService restService;
  private final ObjectUtils objectUtils;
  private final AuditConfiguration auditConfiguration;
  private final AuditUtil auditUtil;

  @Async
  public EventLogDto recordEvent(@NonNull final EventLogDto event) {

    if (!auditConfiguration.isAuditEnabled()) {
      return null;
    }

    try {
      final var url = auditUrl.auditUrl().root();

      final var entity =
          HttpEntityBuilder.builder().jsonContent().body(objectUtils.toString(event)).build();

      final var typeReference = new ParameterizedTypeReference<EventLogDto>() {};

      return restService.post(url, entity, typeReference);
    } catch (IsisException e) {
      log.error(String.format("Error sending audited events: %s", event), e);
    }

    return EventLogDto.builder().build();
  }

  @Async
  public EventLogDto recordEvent(
      @NonNull final String source, @NonNull final String event, Map<String, Object> metadata) {

    final var auditEvent = auditUtil.auditEvent(event, metadata);
    return recordEvent(
        EventLogDto.builder()
            .txnId(UUID.randomUUID().toString())
            .events(List.of(auditEvent))
            .source(source)
            .build());
  }
}
