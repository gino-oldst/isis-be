package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.audit.EventLogDto;
import st.old.isis.enums.Source;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.ReutersXmlExtractionService;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReutersRawDownloadService extends RawDownloadService {

    private final ReutersXmlExtractionService reutersXmlExtractionService;

    private List<String> directoryFilenamePrefixes() {
        return List.of("messages.zip");
    }

    private List<String> unzippedFilenamePrefixes() {
        return List.of("messages.xml");
    }

    @Override
    protected String source() {
        return Source.REUTERS.name();
    }

    @Override
    protected List<S3ObjectSummary> retrieveRawMessageDataFromSource() {
        return null;
    }

    @Override
    protected void processFile(S3Object s3Object, LocalDate importDate, String source) throws IsisException {

        try (final var zis = new ZipInputStream(s3Object.getObjectContent())) {

            var zipEntry = zis.getNextEntry();

            while (zipEntry != null) {

                final var unSkipFile = unzippedFilenamePrefixes().contains(zipEntry.getName());

                if (unSkipFile) {

                    final var clone = cloneInputStream(zis);
                    final var filename = zipEntry.getName();

                    log.info("Process unzipped file: {}", filename);

                    reutersXmlExtractionService.processXml(clone, Source.REUTERS.name(), importDate);

                } else {
                    log.info("Ignore unzipped file: {}", zipEntry.getName());
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();

        } catch (IOException | JAXBException | IsisException e) {

            final var event =
                    auditUtil.auditEvent("REUTERS_UNZIP_FILE_ERROR", Map.of("filename", source));

            auditService.recordEvent(
                    EventLogDto.builder()
                            .txnId(UUID.randomUUID().toString())
                            .events(List.of(event))
                            .source("Extraction Service")
                            .build());

            e.printStackTrace();
            throw IsisException.exception("Error unzipping file: " + source, e);
        }
    }

    protected boolean skipFile(@NonNull String filename) {
        return !directoryFilenamePrefixes().contains(filename);
    }

    protected boolean isDirectoryValid(String key) {
        return key.matches("^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])T\\d{4}\\+\\d{4}$");
    }

    private InputStream cloneInputStream(@NonNull final InputStream inputStream) throws IOException {
        var baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

}
