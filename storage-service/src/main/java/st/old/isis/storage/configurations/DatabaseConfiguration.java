package st.old.isis.storage.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class DatabaseConfiguration {

  @Value("${isis.database.batch-size}")
  private int batchSize;
}
