package st.old.isis.extraction.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.extraction.services.ReutersXmlExtractionService;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/extract/reuters")
public class ReuterXmlExtractionController {

    private final ReutersXmlExtractionService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Upload Reuters/ Refinitiv/ Eikon Data")
    public ListDto<CommunicationChainDto> processXml(@RequestPart MultipartFile file, @RequestPart String dateImported)
            throws JAXBException, IOException {
        return service.processXml(file, LocalDate.parse(dateImported));
    }

}
