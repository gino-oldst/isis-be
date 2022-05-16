package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.extraction.services.internal.NotificationService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class XmlExtractionService {

  private final NotificationService notificationService;

  protected abstract void processReader(
      @NonNull final XMLStreamReader reader, @NonNull final LocalDate importDate, String fileSource)
      throws JAXBException;

  protected abstract void finalizeParsing(String fileSource);

  public void parse(InputStream inputStream, LocalDate importDate, String fileSource)
      throws XMLStreamException, JAXBException {

    final var xmlInputFactory = XMLInputFactory.newInstance();
    final var reader = xmlInputFactory.createXMLStreamReader(inputStream);

    int eventType;

    while (reader.hasNext()) {

      eventType = reader.next();

      if (eventType == XMLEvent.START_ELEMENT) {
        processReader(reader, importDate, fileSource);
      }

      if (eventType == XMLEvent.END_DOCUMENT) {
        finalizeParsing(fileSource);
      }
    }
  }

  protected void sendParsingError(Throwable e) {
    log.error("An error occurred when parsing xml file", e);

    final var request =
        NotificationRequestDto.builder()
            .customMessage(
                MessageDto.builder()
                    .subject("Parsing Failed")
                    .body(String.format("Parsing xml file failed: %s", e.getMessage()))
                    .build())
            // .recipients() todo: recipients
            .build();

    notificationService.sendNotification(List.of(request));
  }
}
