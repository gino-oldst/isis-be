package st.old.isis.extraction.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BatchConfiguration {

  @Value("${isis.download.batch-size}")
  private int batchSize;
}
