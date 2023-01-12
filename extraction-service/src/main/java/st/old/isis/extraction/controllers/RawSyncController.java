package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.dto.util.ProcessRequestDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.amazon.BloombergRawDownloadService;
import st.old.isis.extraction.services.amazon.ReutersRawDownloadService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sync")
public class RawSyncController {

    private final BloombergRawDownloadService bloombergRawDownloadService;
    private final ReutersRawDownloadService reutersRawDownloadService;

    @PostMapping("/bloomberg")
    @Audited(event = "MANUAL_SYNC_BLOOMBERG_S3")
    public void downloadBloomberg(@RequestBody DataImportRecordDto dto) {
        bloombergRawDownloadService.syncAll(dto);
    }

    @PostMapping("/bloomberg/range")
    @Audited(event = "MANUAL_SPECIFIC_DATE_RANGE_SYNC_BLOOMBERG")
    public void downloadBloombergRange(@Audited @RequestBody @NonNull final ProcessRequestDto request) {
        bloombergRawDownloadService.syncByDateRange(request);
    }

    @PostMapping("/reuters")
    @Audited(event = "MANUAL_SYNC_REUTERS_S3")
    public void downloadReuters(@RequestBody DataImportRecordDto dto) {
        reutersRawDownloadService.syncAll(dto);
    }

    @PostMapping("/reuters/range")
    @Audited(event = "MANUAL_SPECIFIC_DATE_RANGE_SYNC_REUTERS")
    public void downloadReutersRange(@Audited @RequestBody @NonNull final ProcessRequestDto request) {
        reutersRawDownloadService.syncByDateRange(request);
    }

}
