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
import st.old.isis.extraction.entities.HistoricData;
import st.old.isis.extraction.services.OpenSearchService;
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

    private final OpenSearchService openSearchService;

    @PostMapping("/download/bloomberg")
    public void downloadBloombergRange(@NonNull @RequestBody final ProcessRequestDto request){
        if(historicDataService.allDatesMissing(request.getDateFrom(), request.getDateTo(), "Bloomberg")){
            System.out.println("all dates missing");
            List<HistoricDataDto> dtoList = historicDataService.bulkCreate(request.getDateFrom(), request.getDateTo(), request.getUserId(), "Bloomberg");
//            bloombergRawDownloadService.historicDownloadByDateRange(request, dtoList);
        }
        else{
            Set<LocalDate> missingDates = historicDataService.missingDates(request.getDateFrom(), request.getDateTo(), "Bloomberg");
            LocalDate[] datesToAdd = missingDates.toArray(new LocalDate[0]);
            System.out.printf("Missing dates %s", missingDates);
            for(int i = 0; i < missingDates.size(); i++){
                HistoricDataDto dto = historicDataService.create(datesToAdd[i], request.getUserId(), "Bloomberg");
//                bloombergRawDownloadService.historicDownload(datesToAdd[i], dto);
            }
        }

    }

    @PostMapping("/download/reuters")
    public void downloadReutersRange(@NonNull @RequestBody final ProcessRequestDto request){

        if(historicDataService.allDatesMissing(request.getDateFrom(), request.getDateTo(), "Reuters")){
            System.out.println("all dates missing");
            List<HistoricDataDto> dtoList =  historicDataService.bulkCreate(request.getDateFrom(), request.getDateTo(), request.getUserId(), "Reuters");
//            reutersRawDownloadService.historicDownloadByDateRange(request, dtoList);
        }
        else{
            Set<LocalDate> missingDates = historicDataService.missingDates(request.getDateFrom(), request.getDateTo(), "Reuters");
            LocalDate[] datesToAdd = missingDates.toArray(new LocalDate[0]);
            for(int i = 0; i < missingDates.size(); i++){
                HistoricDataDto dto = historicDataService.create(datesToAdd[i], request.getUserId(), "Reuters");
//                reutersRawDownloadService.historicDownload(datesToAdd[i], dto);
            }
        }

    }

    @GetMapping("/all")
    public List<HistoricDataDto> getAllData(){
        return historicDataService.getAllEntries();
    }

    @GetMapping("/unexpired-data")
    public List<HistoricDataDto>  getUnexpiredData() {
        return historicDataService.getAllUnexpiredDates();
    }

    @PutMapping("/purge-data")
    public HistoricDataDto purgeHistoricData(@NonNull @RequestBody final HistoricDataDto request){
        openSearchService.purgeByDate(request, request.getDataDate());
        return historicDataService.update(request);
    }

}
