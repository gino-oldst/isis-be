package st.old.isis.storage.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BatchConfiguration {

  @Value("${isis.batch-process.policy-processing.size}")
  private int policyProcessingBatchSize;

  @Value("${isis.batch-process.policy-processing.core-pool-size}")
  private int policyProcessingCorePoolSize;

  @Value("${isis.batch-process.policy-processing.max-pool-size}")
  private int policyProcessingMaxPoolSize;

  @Value("${isis.batch-process.policy-processing.queue-capacity}")
  private int policyProcessingQueueCapacity;

  @Value("${isis.batch-process.policy-processing.reader-page-size}")
  private int policyProcessingReaderPageSizez;

}
