package st.old.isis.extraction.services;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.audit.EventLogDto;
import st.old.isis.enums.Source;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.utils.DownloadUtils;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    return Source.REUTERS.name();
  }

  @Override
  protected String getPrefix(@NonNull final String prefix) {

    final var prefixFormatter = DateTimeFormatter.ofPattern("yyyy-MM-");

    final var now = LocalDate.now();

    if (now.lengthOfMonth() == now.getDayOfMonth()) {
      return now.format(prefixFormatter);
    }

    final var lastFileDate = LocalDate.parse(prefix, DownloadUtils.REUTERS_FILENAME_FORMAT);

    final var lastDateSuffix = lastFileDate.getDayOfMonth() / 10;
    final var currentDateSuffix = now.getDayOfMonth() / 10;

    if (lastDateSuffix == currentDateSuffix) {
      return lastFileDate.format(prefixFormatter) + currentDateSuffix;
    } else {
      return now.format(prefixFormatter);
    }
  }

  @Override
  protected boolean skipFile(@NonNull final String filename) {
    return !directoryFilenamePrefixes().contains(filename);
  }

  @Override
  protected void processFile(
      @NonNull final ShareFileClient client,
      @NonNull final String filename,
      @NonNull final String fileId) {

    unzipFile(client, fileId);
  }

  private List<String> directoryFilenamePrefixes() {
    return List.of("messages.zip");
  }

  private List<String> unzippedFilenamePrefixes() {
    return List.of("messages.xml");
  }

  private void unzipFile(@NonNull final ShareFileClient client, @NonNull final String fileId) {

    final var inputStream = client.openInputStream();
    final var fileSource = client.getFilePath();

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

          processXml(clone, fileSource, filename, fileId);

        } else {
          log.info("Ignore unzipped file: {}", zipEntry.getName());
        }

        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();
    } catch (IOException e) {
      final var event =
          auditUtil.auditEvent("REUTERS_UNZIP_FILE_ERROR", Map.of("filename", fileSource));
      auditService.recordEvent(
          EventLogDto.builder()
              .txnId(UUID.randomUUID().toString())
              .events(List.of(event))
              .source("Extraction Service")
              .build());
      log.error("Error unzipping file: {} - {}", fileSource, e.getMessage());
      e.printStackTrace();
    }
  }

  private InputStream cloneInputStream(@NonNull final InputStream inputStream) throws IOException {
    var baos = new ByteArrayOutputStream();
    inputStream.transferTo(baos);
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private void processXml(
      @NonNull final InputStream inputStream,
      @NonNull final String fileSource,
      @NonNull final String filename,
      @NonNull final String fileId) {
    try {
      extractionService.processXml(inputStream, fileSource);

      saveToHistory(fileId, fileSource);

    } catch (JAXBException | IsisException e) {
      final var event =
          auditUtil.auditEvent("REUTERS_FILE_PARSING_ERROR", Map.of("filename", filename));
      auditService.recordEvent(
          EventLogDto.builder()
              .txnId(UUID.randomUUID().toString())
              .events(List.of(event))
              .source("Extraction Service")
              .build());
      log.error("Error processing file: {} - {}", filename, e.getMessage());
      e.printStackTrace();
    }
  }
}
