package st.old.isis.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AuditConfiguration {

    @Value("${isis.audit.enabled:false}")
    private boolean auditEnabled;
}
