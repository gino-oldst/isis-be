package st.old.isis.storage.batch.systemevents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.utils.ListUtils;

import java.util.List;

@Slf4j
public class SystemEventsWriter implements ItemWriter<CommunicationMessageDto> {

    @Autowired private CommunicationMessageService communicationMessageService;

    @Override
    public void write(List<? extends CommunicationMessageDto> list) throws Exception {
        if(!ListUtils.isNullOrEmpty(list)) {
            log.info("Chunk started! - Request Size: {}", list.size());
            final var results = communicationMessageService.updateBypassMessageType((List<CommunicationMessageDto>) list);
            log.info("Chunk completed! - Results Size:{}", list.size(), results);
        }
    }
}
