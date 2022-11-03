package st.old.isis.storage.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import st.old.isis.storage.batch.systemevents.SystemMessageBatchConfig;

@Configuration
@EnableBatchProcessing
public class BatchAppConfig {

    @Bean
    @Qualifier("policyProcessingBatchJob")
    public ApplicationContextFactory processPolicyProcessingBatchJobs(){
        return new GenericApplicationContextFactory(MessageItemBatchConfig.class);
    }

    @Bean
    @Qualifier("systemEventsBatchJob")
    public ApplicationContextFactory processSystemEventsBatchJobs(){
        return new GenericApplicationContextFactory(SystemMessageBatchConfig.class);
    }

}
