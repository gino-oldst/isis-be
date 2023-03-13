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
import st.old.isis.extraction.services.HistoricDataService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/historic-data")
public class HistoricDataController {
    private final BloombergRawDownloadService bloombergRawDownloadService;
    private final ReutersRawDownloadService reutersRawDownloadService;
    private final HistoricDataService historicDataService;

    @PostMapping("/download/bloomberg")
    public void downloadBloombergRange(@NonNull @RequestBody final ProcessRequestDto request){
        if(historicDataService.allDatesMissing(request.getDateFrom(), request.getDateTo()))
            bloombergRawDownloadService.syncByDateRange(request);
    }

    @PostMapping("/download/reuters")
    public void downloadReutersRange(@NonNull @RequestBody final ProcessRequestDto request){
        if(historicDataService.allDatesMissing(request.getDateFrom(),request.getDateTo()))
            reutersRawDownloadService.syncByDateRange(request);
    }


}
