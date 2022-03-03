package st.old.isis.extraction.services;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class ReutersDownloadService extends AzureDownloadService {

  private final ReutersXmlExtractionService extractionService;

  public ReutersDownloadService(
      @NonNull @Qualifier("Reuters") final ShareDirectoryClient reuters,
      @NonNull final ReutersXmlExtractionService extractionService) {
    super(reuters);
    this.extractionService = extractionService;
  }

  @Override
  @Audited(event = "DOWNLOAD_REUTERS_RAW_DATA")
  public void sync() {
    super.sync();
  }

  @Override
  @Audited(event = "DOWNLOAD_SPECIFIC_REUTERS_RAW_DATA")
  public void sync(@NonNull @Audited final String prefix) {
    super.sync(prefix);
  }

  @Override
  protected String source() {
    return "Reuters";
  }

  @Override
  protected boolean skipFile(@NonNull final String filename) {
    return !directoryFilenamePrefixes().contains(filename);
  }

  @Override
  protected void processFile(@NonNull final ShareFileClient client) {

    final var inputStream = client.openInputStream();

    unzipFile(inputStream);
  }

  private List<String> directoryFilenamePrefixes() {
    return List.of("messages.zip");
  }

  private List<String> unzippedFilenamePrefixes() {
    return List.of("messages.xml");
  }

  private void unzipFile(@NonNull final InputStream inputStream) {

    try {

      final var zis = new ZipInputStream(inputStream);

      var zipEntry = zis.getNextEntry();

      while (zipEntry != null) {

        final var unSkipFile = unzippedFilenamePrefixes().contains(zipEntry.getName());

        if (unSkipFile) {

          final var clone = cloneInputStream(zis);
          final var filename = zipEntry.getName();

          log.info("Process unzipped file: {}", filename);

          auditUtil.audit("REUTERS_UNZIP_FILE", Map.of("filename", filename));

          processXml(clone);

        } else {
          log.info("Ignore unzipped file: {}", zipEntry.getName());
        }

        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private InputStream cloneInputStream(@NonNull final InputStream inputStream) throws IOException {
    var baos = new ByteArrayOutputStream();
    inputStream.transferTo(baos);
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private void processXml(@NonNull final InputStream inputStream) {
    try {
      extractionService.processXml(inputStream);
    } catch (JAXBException e) {
      log.error("Error processing xml file: {}", e.getMessage());
      e.printStackTrace();
    }
  }
}
