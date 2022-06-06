package st.old.isis.storage.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import st.old.isis.dto.storage.BatchAlertNotificationDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MessageItemWriter implements ItemWriter<BatchAlertNotificationDto> {

    @Override
    public void write(List<? extends BatchAlertNotificationDto> list) throws Exception {

        // Only used for logging

//        list.stream().forEach(notif -> {
//            if(notif.getCount() > 0) log.info("Batch Alert Notification for message id {} with {} alerts", notif.getCommMessageId(), notif.getCount());
//        });

        log.info("Chunk completed! Size: {}", list.size());
    }
}
