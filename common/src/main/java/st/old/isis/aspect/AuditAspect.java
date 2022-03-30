package st.old.isis.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import st.old.isis.annotations.Audited;
import st.old.isis.configuration.AuditConfiguration;
import st.old.isis.context.AuditContext;
import st.old.isis.dto.audit.AuditEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

  private final AuditConfiguration auditConfiguration;
  private final ObjectMapper mapper;

  @Around("@annotation(st.old.isis.annotations.Audited)")
  public Object processAudit(ProceedingJoinPoint joinPoint) throws Throwable {
    return audit(joinPoint);
  }

  private Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

    if (!(joinPoint.getSignature() instanceof MethodSignature)) {
      return joinPoint.proceed();
    }

    if (!auditConfiguration.isAuditEnabled()) {
      return joinPoint.proceed();
    }

    if (!AuditContext.hasStarted()) {
      return joinPoint.proceed();
    }

    log.debug("Recording event");

    final var signature = (MethodSignature) joinPoint.getSignature();
    final var method = signature.getMethod();

    final var auditMethod = method.getAnnotation(Audited.class);

    final var description = auditMethod.description();

    final var event =
        AuditEvent.builder()
            .event(auditMethod.event())
            .description(description.isEmpty() ? null : description)
            .dateCreated(LocalDateTime.now())
            .occurrence(new AtomicInteger(1))
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

    AuditContext.addEvent(event);

    return joinPoint.proceed();
  }
}
