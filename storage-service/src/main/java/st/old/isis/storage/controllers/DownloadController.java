package st.old.isis.storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.communication.QuickCommunicationDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.services.QuickCommunicationService;
import st.old.isis.utils.CsvUtil;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/download")
public class DownloadController {

    private final CommunicationMessageService communicationMessageService;
    private final QuickCommunicationService quickCommunicationService;

    @PostMapping(path = "/messages")
    public void getMessages(@RequestBody CommunicationMessageDto filter,
                                    HttpServletResponse response) {

        final var results = communicationMessageService.downloadMessages(filter);
        CsvUtil.export(response, "messages-" + LocalDateTime.now().toString().concat(".csv"),
                CommunicationMessageDto.csvHeaders(), results);
    }

    @PostMapping(path = "/alerts")
    public void getAlerts(@RequestBody QuickCommunicationDto filter,
                            HttpServletResponse response) throws IsisException {

        final var results = quickCommunicationService.downloadAlerts(filter);
        CsvUtil.exportList(response, "alerts-" + LocalDateTime.now().toString().concat(".csv"),
                QuickCommunicationDto.csvHeaders(), results);
    }
}
