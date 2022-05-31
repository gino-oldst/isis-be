package st.old.isis.storage.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import st.old.isis.storage.entities.AlertLog;

import java.util.List;

@Slf4j
public class MessageItemWriter implements ItemWriter<AlertLog> {

    @Override
    public void write(List<? extends AlertLog> list) throws Exception {

        log.info("Processing list of alerts for message id {}", list.get(0).getCommMessageId());

//        list.stream().forEach(message -> {
//            log.info("Done Processing message {}", message);
//        });

        log.info("Chunk completed!");

    }
}
