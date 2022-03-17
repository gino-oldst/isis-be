package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.CommunicationChainService;
import st.old.isis.storage.services.internal.NotificationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chain")
public class CommunicationChainController {

    private final CommunicationChainService service;
    private final NotificationService notificationService;

    @GetMapping("/{id}")
    public CommunicationChainDto getById(@PathVariable final String id) throws IsisException {
        return service.getById(id);
    }

    @PostMapping("/search")
    public ListDto<CommunicationChainDto> search(@RequestBody final CommunicationChainDto filter) {
        return service.search(filter);
    }

    @PostMapping("/search/paged")
    public ListDto<CommunicationChainDto> pagedSearch(@RequestBody final CommunicationChainDto filter, Pageable pageable) {
        return service.search(filter, pageable);
    }

    @PostMapping("/import")
    public ListDto<CommunicationChainDto> importData(
            @RequestBody final ListDto<CommunicationChainDto> request) throws IsisException {
        try {
            return service.importData(request);
        } catch (Throwable err) {

            log.error("Error importing parsed data", err);

            final var notification =
                    NotificationRequestDto.builder()
                            .customMessage(
                                    MessageDto.builder()
                                            .subject("Saving Parsed Data Failed")
                                            .body(String.format("Saving parsed data failed: %s", err.getMessage()))
                                            .build())
                            // .recipients() todo: recipients
                            .build();

            notificationService.sendNotification(List.of(notification));

            throw err;
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final String id) throws IsisException {
        service.delete(id);
    }
}
