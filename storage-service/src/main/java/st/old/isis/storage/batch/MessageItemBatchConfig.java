package st.old.isis.storage.batch;

import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import st.old.isis.dto.storage.BatchAlertNotificationDto;
import st.old.isis.storage.configurations.BatchConfiguration;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.policy.PolicyProcessorService;
import st.old.isis.storage.repositories.AlertLogRepository;
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
@RequiredArgsConstructor
public class MessageItemBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final CommunicationMessageRepository communicationMessageRepository;

    private final BatchConfiguration batchConfiguration;

    @Bean
    @StepScope
    public RepositoryItemReader<CommunicationMessage> reader(   @Value("#{jobParameters['source']}") String source,
                                                                @Value("#{jobParameters['dateFrom']}") String dateFrom,
                                                                @Value("#{jobParameters['dateTo']}")String dateTo) {
        RepositoryItemReader<CommunicationMessage> reader = new RepositoryItemReader<>();
        reader.setRepository(communicationMessageRepository);
        reader.setMethodName("findAllBySourceAndImportDateBetween");

        List<Object> queryMethodArguments = new ArrayList<>();
        queryMethodArguments.add(source);
        queryMethodArguments.add(LocalDate.parse(dateFrom));
        queryMethodArguments.add(LocalDate.parse(dateTo));
        reader.setArguments(queryMethodArguments);

        reader.setPageSize(100);
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("timeSent", Sort.Direction.ASC);
        reader.setSort(sorts);

        return reader;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(batchConfiguration.getPolicyProcessingCorePoolSize());
        executor.setMaxPoolSize(batchConfiguration.getPolicyProcessingMaxPoolSize());
        executor.setQueueCapacity(batchConfiguration.getPolicyProcessingQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("MultiThreaded-");
        return executor;
    }

    @Bean
    public MessageItemWriter writer() {
        return new MessageItemWriter();
    }

    @Bean
    public MessageItemProcessor processor() {
        return new MessageItemProcessor();
    }

    @Bean
    public AsyncItemProcessor<CommunicationMessage, BatchAlertNotificationDto> asyncProcessor() {
        AsyncItemProcessor<CommunicationMessage, BatchAlertNotificationDto> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(processor());
        asyncItemProcessor.setTaskExecutor(taskExecutor());
        return asyncItemProcessor;
    }

    @Bean
    public AsyncItemWriter<BatchAlertNotificationDto> asyncWriter() {
        AsyncItemWriter<BatchAlertNotificationDto> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(writer());
        return asyncItemWriter;
    }

    @Bean
    public Step step1(ItemReader<CommunicationMessage> itemReader, ItemWriter<BatchAlertNotificationDto> itemWriter)
            throws Exception {
        return this.stepBuilderFactory
                .get("step1")
                .<CommunicationMessage, Future<BatchAlertNotificationDto>>chunk(batchConfiguration.getPolicyProcessingBatchSize())
                .reader(itemReader)
                .processor(asyncProcessor())
                .writer(asyncWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job profileUpdateJob(JobCompletionNotificationListener listener, Step step1)
            throws Exception {

        return this.jobBuilderFactory.get("policyProcessorBatchJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end().build();
    }


}
