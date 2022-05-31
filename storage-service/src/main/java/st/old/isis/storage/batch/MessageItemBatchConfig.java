package st.old.isis.storage.batch;

import lombok.RequiredArgsConstructor;
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
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.repositories.AlertLogRepository;
import st.old.isis.storage.repositories.CommunicationMessageRepository;

import java.time.LocalDate;
import java.util.*;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class MessageItemBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final CommunicationMessageRepository communicationMessageRepository;
    private final AlertLogRepository alertLogRepository;

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
    public RepositoryItemWriter<AlertLog> writer() {
        RepositoryItemWriter<AlertLog> writer = new RepositoryItemWriter<>();
        writer.setRepository(alertLogRepository);
        writer.setMethodName("save");
        return writer;
    }


    @Bean
    public MessageItemProcessor processor() {
        return new MessageItemProcessor();
    }

    @Bean
    public Step step1(ItemReader<CommunicationMessage> itemReader, ItemWriter<AlertLog> itemWriter)
            throws Exception {
        return this.stepBuilderFactory.get("step1").<CommunicationMessage, AlertLog>chunk(100).reader(itemReader)
                .processor(processor()).writer(itemWriter).build();
    }

    @Bean
    public Job profileUpdateJob(JobCompletionNotificationListener listener, Step step1)
            throws Exception {

        return this.jobBuilderFactory.get("profileUpdateJob").incrementer(new RunIdIncrementer())
                .listener(listener).start(step1).build();
    }


}
