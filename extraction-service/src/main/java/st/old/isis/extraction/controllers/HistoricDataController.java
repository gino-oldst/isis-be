package st.old.isis.extraction.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.dto.storage.HistoricDataDto;
import st.old.isis.dto.util.ProcessRequestDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.services.amazon.BloombergRawDownloadService;
import st.old.isis.extraction.services.amazon.ReutersRawDownloadService;
import st.old.isis.extraction.services.HistoricDataService;

import java.util.List;
import java.util.Set;
import java.time.LocalDate;

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
        if(historicDataService.allDatesMissing(request.getDateFrom(), request.getDateTo(), "Bloomberg")){
            System.out.println("all dates missing");
            historicDataService.bulkCreate(request.getDateFrom(), request.getDateTo(), request.getUserId(), "Bloomberg");
            bloombergRawDownloadService.syncByDateRange(request);
        }
        else{
            Set<LocalDate> missingDates = historicDataService.missingDates(request.getDateFrom(), request.getDateTo(), "Bloomberg");
            LocalDate[] datesToAdd = missingDates.toArray(new LocalDate[0]);
            System.out.printf("Missing dates %s", missingDates);
            for(int i = 0; i < missingDates.size(); i++){
                historicDataService.create(datesToAdd[i], request.getUserId(), "Bloomberg");
                bloombergRawDownloadService.syncDate(datesToAdd[i]);
            }
        }
    }

    @PostMapping("/download/reuters")
    public void downloadReutersRange(@NonNull @RequestBody final ProcessRequestDto request){
        if(historicDataService.allDatesMissing(request.getDateFrom(), request.getDateTo(), "Reuters")){
            System.out.println("all dates missing");
            historicDataService.bulkCreate(request.getDateFrom(), request.getDateTo(), request.getUserId(), "Reuters");
            reutersRawDownloadService.syncByDateRange(request);
        }
        else{
            Set<LocalDate> missingDates = historicDataService.missingDates(request.getDateFrom(), request.getDateTo(), "Reuters");
            LocalDate[] datesToAdd = missingDates.toArray(new LocalDate[0]);
            for(int i = 0; i < missingDates.size(); i++){
                historicDataService.create(datesToAdd[i], request.getUserId(), "Reuters");
                reutersRawDownloadService.syncDate(datesToAdd[i]);
            }
        }
    }

    @GetMapping("/all")
    public List<HistoricDataDto> getAllData(){
        return historicDataService.getAllEntries();
    }

    @GetMapping("/unexpired-data")
    public List<LocalDate>  getUnexpiredData() {
        return historicDataService.getAllUnexpiredDates();
    }


}
