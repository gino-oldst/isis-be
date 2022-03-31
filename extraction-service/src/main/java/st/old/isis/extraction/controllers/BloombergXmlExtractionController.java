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
import st.old.isis.extraction.services.BloombergChatExtractionService;
import st.old.isis.extraction.services.BloombergEmailExtractionService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/extract/bloomberg")
public class BloombergXmlExtractionController {

  private final BloombergChatExtractionService chatExtractionService;
  private final BloombergEmailExtractionService emailExtractionService;

  @PostMapping(path = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(description = "Upload Bloomberg Chat Data")
  public void processChatXml(@RequestPart MultipartFile file, @RequestPart String dateImported)
      throws JAXBException, IOException, XMLStreamException {
    chatExtractionService.processXml(file, LocalDate.parse(dateImported));
  }

  @PostMapping(path = "/email", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(description = "Upload Bloomberg Email Data")
  public void processEmailXml(@RequestPart MultipartFile file, @RequestPart String dateImported)
      throws JAXBException, IOException, XMLStreamException {
    emailExtractionService.processXml(file, LocalDate.parse(dateImported));
  }
}
