package st.old.isis.storage.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import st.old.isis.dto.storage.BatchAlertNotificationDto;
import st.old.isis.storage.configurations.BatchConfiguration;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.repositories.CommunicationMessageRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableBatchProcessing
@Slf4j
public class MessageItemBatchConfig {

    @Autowired private JobBuilderFactory jobBuilderFactory;
    @Autowired private StepBuilderFactory stepBuilderFactory;
    @Autowired private CommunicationMessageRepository communicationMessageRepository;
    @Autowired private BatchConfiguration batchConfiguration;
    @Bean
    @StepScope
    public RepositoryItemReader<CommunicationMessage> batchPolicyReader(@Value("#{jobParameters['source']}") String source,
                                                                        @Value("#{jobParameters['dateFrom']}") String dateFrom,
                                                                        @Value("#{jobParameters['dateTo']}")String dateTo) {
        RepositoryItemReader<CommunicationMessage> reader = new RepositoryItemReader<>();

        List<Object> queryMethodArguments = new ArrayList<>();

        if(source != null && !source.isEmpty()){
            reader.setMethodName("findAllBySourceAndBypassTypeIsNullAndImportDateBetween");
            queryMethodArguments.add(source);
        } else {
            reader.setMethodName("findAllByBypassTypeIsNullAndImportDateBetween");
        }

        reader.setRepository(communicationMessageRepository);

        queryMethodArguments.add(LocalDate.parse(dateFrom));
        queryMethodArguments.add(LocalDate.parse(dateTo));

        reader.setArguments(queryMethodArguments);

        reader.setPageSize(batchConfiguration.getPolicyProcessingReaderPageSize());
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("timeSent", Sort.Direction.ASC);
        reader.setSort(sorts);
        return reader;
    }

    @Bean
    public TaskExecutor batchPolicyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(batchConfiguration.getPolicyProcessingCorePoolSize());
        executor.setMaxPoolSize(batchConfiguration.getPolicyProcessingMaxPoolSize());
        executor.setQueueCapacity(batchConfiguration.getPolicyProcessingQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("MultiThreaded-");
        return executor;
    }

    @Bean
    public MessageItemWriter batchPolicyWriter() {
        return new MessageItemWriter();
    }

    @Bean
    public MessageItemProcessor batchPolicyProcessor() {
        return new MessageItemProcessor();
    }

    @Bean
    public AsyncItemProcessor<CommunicationMessage, BatchAlertNotificationDto> batchPolicyAsyncProcessor() {
        AsyncItemProcessor<CommunicationMessage, BatchAlertNotificationDto> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(batchPolicyProcessor());
        asyncItemProcessor.setTaskExecutor(batchPolicyTaskExecutor());
        return asyncItemProcessor;
    }

    @Bean
    public AsyncItemWriter<BatchAlertNotificationDto> batchPolicyAsyncWriter() {
        AsyncItemWriter<BatchAlertNotificationDto> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(batchPolicyWriter());
        return asyncItemWriter;
    }



    @Bean
    public AlertBatchStepExecutionListener systemEventsInitializeStepExecutionListener() throws Exception {
        return new AlertBatchStepExecutionListener();
    }

    @Bean
    public Step batchPolicyJobStep1(ItemReader<CommunicationMessage> batchPolicyReader, ItemWriter<BatchAlertNotificationDto> batchPolicyWriter)
            throws Exception {
        return this.stepBuilderFactory
                .get("batchPolicyJobStep1")
                .<CommunicationMessage, Future<BatchAlertNotificationDto>>chunk(batchConfiguration.getPolicyProcessingBatchSize())
                .reader(batchPolicyReader)
                .processor(batchPolicyAsyncProcessor())
                .writer(batchPolicyAsyncWriter())
                .listener(systemEventsInitializeStepExecutionListener())
                .listener(batchPolicyProcessor())
                .taskExecutor(batchPolicyTaskExecutor())
                .build();
    }

    @Bean
    public Job batchPolicyJob(JobCompletionNotificationListener listener, Step batchPolicyJobStep1) {

        return this.jobBuilderFactory.get("batchPolicyJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(batchPolicyJobStep1)
                .end().build();
    }


}
