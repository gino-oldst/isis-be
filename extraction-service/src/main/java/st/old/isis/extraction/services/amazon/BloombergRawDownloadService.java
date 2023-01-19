package st.old.isis.extraction.services.amazon;

import com.amazonaws.services.iotevents.model.Input;
import com.amazonaws.services.s3.model.S3Object;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.dto.audit.EventLogDto;
import st.old.isis.enums.Source;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.BloombergChatExtractionService;
import st.old.isis.extraction.services.BloombergEmailExtractionService;
import st.old.isis.extraction.utils.DownloadUtils;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloombergRawDownloadService extends RawDownloadService {

    private final BloombergChatExtractionService bloombergChatExtractionService;
    private final BloombergEmailExtractionService emailExtractionService;

    private final Pattern CHAT_FILE_PATTERN = Pattern.compile("^f840690.ib19.*\\.xml$");
    private final Pattern EMAIL_FILE_PATTERN = Pattern.compile("^f840690.msg.*\\.xml$");

    @Override
    protected String source() {
        return Source.BLOOMBERG.name();
    }

    @Override
    protected void processFile(S3Object s3Object, LocalDate importDate, String source) throws IsisException {

        String objectKey = s3Object.getKey();
        final var keys = objectKey.split(DownloadUtils.S3_DELIMITER);

        String fileName = getS3FileName(objectKey);

        try {

            if (isChat(fileName)) {
                // TODO (Optional) Will set import date manual for now, replace parse with processXML once scheduled jobs is activated
//        bloombergChatExtractionService.processXml(s3Object.getObjectContent(), source);
                bloombergChatExtractionService.parse(s3Object.getObjectContent(), importDate, source);
            }

            if(isEmail(fileName)){
                // TODO (Optional) Will set import date manual for now, replace parse with processXML once scheduled jobs is activated
//            emailExtractionService.processXml(s3Object.getObjectContent(), source);
                emailExtractionService.parse(s3Object.getObjectContent(), importDate, source);
            }

        } catch (JAXBException | XMLStreamException e) {

            final var event =
                    auditUtil.auditEvent("BLOOMBERG_UNZIP_FILE_ERROR", Map.of("filename", source));

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
    @Override
    protected void processInputStream(InputStream inputStream, String fileName, LocalDate importDate, String source) throws IsisException {

        try {

            if (isChat(fileName)) {
                // TODO (Optional) Will set import date manual for now, replace parse with processXML once scheduled jobs is activated
//        bloombergChatExtractionService.processXml(s3Object.getObjectContent(), source);
                bloombergChatExtractionService.parse(inputStream, importDate, source);
            }

            if(isEmail(fileName)){
                // TODO (Optional) Will set import date manual for now, replace parse with processXML once scheduled jobs is activated
//            emailExtractionService.processXml(s3Object.getObjectContent(), source);
                emailExtractionService.parse(inputStream, importDate, source);
            }

        } catch (JAXBException | XMLStreamException e) {

            final var event =
                    auditUtil.auditEvent("BLOOMBERG_UNZIP_FILE_ERROR", Map.of("filename", source));

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
        return filenamePatterns().stream().noneMatch(pattern -> pattern.matcher(filename).find());
    }

    protected boolean isDirectoryValid(String key) {
        return key.matches("^[0-9]{4,6}$");
    }

    @Override
    protected LocalDate parseToImportDate(String key) {
        final String rawDate = getFileDate(key);
        return LocalDate.parse(rawDate, DownloadUtils.BLOOMBERG_FILENAME_FORMAT);
    }

    @Override
    protected String parseToS3FolderDate(LocalDate originalDate) {
        return originalDate.format(DownloadUtils.BLOOMBERG_FILENAME_FORMAT);
    }

    private InputStream cloneInputStream(@NonNull final InputStream inputStream) throws IOException {
        var baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private List<Pattern> filenamePatterns() {
        return List.of(CHAT_FILE_PATTERN, EMAIL_FILE_PATTERN);
    }

    private boolean isChat(@NonNull final String filename) {
        return CHAT_FILE_PATTERN.matcher(filename).find();
    }

    private boolean isEmail(@NonNull final String filename) {
        return EMAIL_FILE_PATTERN.matcher(filename).find();
    }

}
