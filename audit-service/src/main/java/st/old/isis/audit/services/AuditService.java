package st.old.isis.audit.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.audit.entities.EventLog;
import st.old.isis.audit.repositories.EventLogRepository;
import st.old.isis.dto.audit.EventLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.utils.ObjectUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final EventLogRepository repository;
  private final ObjectUtils objectUtils;

  public EventLogDto record(@NonNull final EventLogDto event) throws IsisException {

    final var entity = mapToEntity(event);

    final var recordedEvent = repository.save(entity);

    return event;
  }

  private EventLog mapToEntity(@NonNull final EventLogDto resource) throws IsisException {
    return EventLog.builder()
        .txnId(resource.getTxnId())
        .source(resource.getSource())
        .metadata(objectUtils.toString(resource.getEvents()))
        .build();
  }
}
