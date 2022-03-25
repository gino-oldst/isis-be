package st.old.isis.extraction.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.extraction.services.internal.NotificationService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamReader;
import java.time.LocalDate;

@Slf4j
@Service
public class BloombergEmailExtractionService extends XmlExtractionService {

  public BloombergEmailExtractionService(NotificationService notificationService) {
    super(notificationService);
  }

  @Override
  protected void processReader(XMLStreamReader reader, LocalDate importDate) throws JAXBException {
    // TODO
  }

  @Override
  protected void finalizeParsing() {
    // TODO
  }
}
