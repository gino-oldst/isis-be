package st.old.isis.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import st.old.isis.annotations.Audited;
import st.old.isis.context.AuditContext;
import st.old.isis.dto.audit.AuditEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

  @Value("${isis.audit.enabled:false}")
  private boolean auditEnabled;

  private final ObjectMapper mapper;

  @Around("@annotation(st.old.isis.annotations.Audited)")
  public Object processAudit(ProceedingJoinPoint joinPoint) throws Throwable {
    return audit(joinPoint);
  }

  private Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

    if (!(joinPoint.getSignature() instanceof MethodSignature)) {
      return joinPoint.proceed();
    }

    if (!auditEnabled) {
      return joinPoint.proceed();
    }

    if (!AuditContext.hasStarted()) {
      return joinPoint.proceed();
    }

    log.debug("Recording event");

    final var eventLog = AuditContext.getAuditContext();

    final var signature = (MethodSignature) joinPoint.getSignature();
    final var method = signature.getMethod();

    final var auditMethod = method.getAnnotation(Audited.class);

    final var event =
        AuditEvent.builder()
            .event(auditMethod.event())
            .description(auditMethod.description())
            .dateCreated(LocalDateTime.now())
            .build();

    final var parameters = method.getParameters();

    final var metadata = new HashMap<String, Object>();

    // get method arguments/parameters
    final var args = joinPoint.getArgs();

    for (int x = 0; x < parameters.length; x++) {
      int position = x;
      Optional.ofNullable(parameters[x].getAnnotation(Audited.class))
          .ifPresent((a) -> metadata.put(parameters[position].getName(), args[position]));
    }

    if (!metadata.isEmpty()) {
      event.setMetadata(mapper.writeValueAsString(metadata));
    }

    eventLog.getEvents().add(event);

    return joinPoint.proceed();
  }
}
