package st.old.isis.storage.services.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import st.old.isis.configuration.AuditConfiguration;
import st.old.isis.dto.audit.EventLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.builder.AuditUrl;
import st.old.isis.utils.HttpEntityBuilder;
import st.old.isis.utils.ObjectUtils;

@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditUrl auditUrl;
  private final InternalRestService restService;
  private final ObjectUtils objectUtils;
  private final AuditConfiguration auditConfiguration;

  @Async
  public EventLogDto recordEvent(@NonNull final EventLogDto event) throws IsisException {

    if (!auditConfiguration.isAuditEnabled()) {
      return null;
    }

    final var url = auditUrl.auditUrl().root();

    final var entity =
        HttpEntityBuilder.builder().jsonContent().body(objectUtils.toString(event)).build();

    final var typeReference = new ParameterizedTypeReference<EventLogDto>() {};

    return restService.post(url, entity, typeReference);
  }
}
