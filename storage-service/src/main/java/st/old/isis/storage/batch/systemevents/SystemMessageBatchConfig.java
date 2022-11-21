package st.old.isis.storage.batch.systemevents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.storage.batch.AlertBatchStepExecutionListener;
import st.old.isis.storage.batch.JobCompletionNotificationListener;
import st.old.isis.storage.configurations.BatchConfiguration;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.repositories.CommunicationMessageRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableBatchProcessing
@Slf4j
public class SystemMessageBatchConfig {

    @Autowired private JobBuilderFactory jobBuilderFactory;

    @Autowired private StepBuilderFactory stepBuilderFactory;

    @Autowired private CommunicationMessageRepository communicationMessageRepository;

    @Autowired private BatchConfiguration batchConfiguration;

    private final int JOB_SPECIFIC_CORE_POOL_SIZE = 1;
    private final int JOB_SPECIFIC_MAX_POOL_SIZE = 1;
    private final int JOB_SPECIFIC_QUEUE_CAPACITY = 2000;
    private final int JOB_SPECIFIC_READER_PAGE_SIZE= 10000;
    private final int JOB_SPECIFIC_PROCESSING_BATCH_SIZE= 2000;

    @Bean
    public TaskExecutor systemEventsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(JOB_SPECIFIC_CORE_POOL_SIZE);
        executor.setMaxPoolSize(JOB_SPECIFIC_MAX_POOL_SIZE);
        executor.setQueueCapacity(JOB_SPECIFIC_QUEUE_CAPACITY);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("MultiThreaded-");
        return executor;
    }

    @Bean
    @StepScope
    public RepositoryItemReader<CommunicationMessage> systemEventsReader(@Value("#{jobParameters['source']}") String source,
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

        reader.setPageSize(JOB_SPECIFIC_READER_PAGE_SIZE);
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("timeSent", Sort.Direction.ASC);
        reader.setSort(sorts);
        return reader;
    }

    @Bean
    public SystemEventsBatchProcessor systemEventsProcessor() {
        return new SystemEventsBatchProcessor();
    }

    @Bean
    public SystemEventsWriter systemEventsWriter() {
        return new SystemEventsWriter();
    }

    @Bean
    public AlertBatchStepExecutionListener systemEventsInitializeStepExecutionListener() throws Exception {
        return new AlertBatchStepExecutionListener();
    }

    @Bean
    public Step systemEventsStep1(ItemReader<CommunicationMessage> systemEventsReader, ItemWriter<CommunicationMessageDto> systemEventsWriter)
            throws Exception {
        return this.stepBuilderFactory
                .get("systemEventsStep1")
                .<CommunicationMessage, CommunicationMessageDto>chunk(JOB_SPECIFIC_PROCESSING_BATCH_SIZE)
                .reader(systemEventsReader)
                .processor(systemEventsProcessor())
                .writer(systemEventsWriter())
                .listener(systemEventsInitializeStepExecutionListener())
                .listener(systemEventsProcessor())
                .taskExecutor(systemEventsTaskExecutor())
                .build();
    }

    @Bean
    public Job systemEventsJob(JobCompletionNotificationListener listener, Step systemEventsStep1) {
        return this.jobBuilderFactory.get("systemEventsJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(systemEventsStep1)
                .end().build();
    }



}
