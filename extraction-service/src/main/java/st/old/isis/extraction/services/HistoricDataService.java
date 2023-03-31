package st.old.isis.extraction.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.HistoricDataDto;
import st.old.isis.exception.IsisException;
import st.old.isis.extraction.entities.HistoricData;
import st.old.isis.extraction.repositories.HistoricDataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricDataService {

    private final HistoricDataRepository repository;

    public boolean allDatesMissing(LocalDate dateFrom, LocalDate dateTo, String source){
        Set<LocalDate> missingDates = missingDates(dateFrom, dateTo, source);
        System.out.printf("Missing dates %s\n", missingDates);
        List<LocalDate> datesBetween = getDateRange(dateFrom, dateTo);
        Set<LocalDate> allDates = new HashSet<>(datesBetween);
        //System.out.printf("dates to import %s\n", allDates);
        allDates.removeAll(missingDates);
//        System.out.println(missingDates);
        if (allDates.size() > 0) return false;

        return true;
    }

    public HistoricDataDto createEntity(@NonNull @Audited final HistoricDataDto dto){
        final var entity = mapToEntity(dto);
        final var createdEntity = repository.save(entity);

        return mapToDto(createdEntity);
    }

    public HistoricDataDto create(@NonNull @Audited final LocalDate date, String userId, String source){
        LocalDate today = LocalDate.now();
        LocalDate expiry = today.plusMonths(1);
        HistoricDataDto dto = HistoricDataDto.builder().
                                userId(userId).
                                dataDate(date).
                                dateExpiry(expiry).
                                dateImported(today).
                                source(source).
                                expired(false).
                                build();

        createEntity(dto);
        return dto;
    }

    public List<LocalDate> getAllUnexpiredDates(){
        List<HistoricData> unexpired = repository.findByExpired(false);
        List<LocalDate> outputList = new ArrayList<>();
        for(int i = 0; i< unexpired.size(); i++){
            outputList.add(unexpired.get(i).getDataDate());
        }
        return outputList;
    }

    public List<HistoricDataDto> bulkCreate(LocalDate dateFrom, LocalDate dateTo, String userId, String source){
        List<LocalDate> allDates = getDateRange(dateFrom, dateTo);
        List<HistoricDataDto> returnDto = new ArrayList<HistoricDataDto>();
        LocalDate today = LocalDate.now();
        LocalDate expiry = today.plusMonths(1);
        for(int i = 0; i < allDates.size(); i++){
            HistoricDataDto dto = HistoricDataDto.builder().
                                    userId(userId).
                                    dataDate(allDates.get(i)).
                                    dateExpiry(expiry).
                                    dateImported(today).
                                    expired(false).
                                    source(source).
                                    build();
            returnDto.add(createEntity(dto));
        }

        return returnDto;
    }

    public Set<LocalDate> missingDates(LocalDate dateFrom, LocalDate dateTo, String source){
        List<LocalDate> datesBetween = getDateRange(dateFrom, dateTo);
        List<HistoricData> availableData = repository.findByDataDateBetween(dateFrom, dateTo);
        List<LocalDate> datesAvailable = new ArrayList<LocalDate>();
        for(int i = 0; i<availableData.size(); i++){
            HistoricDataDto dataDto = mapToDto(availableData.get(i));
            if (dataDto.getSource().equalsIgnoreCase(source)) {
                datesAvailable.add(dataDto.getDataDate());
            }
        }

        System.out.printf("Dates available %s\n", datesAvailable);
        System.out.printf("Difference of data %s\n", findDifference(datesBetween, datesAvailable));

        return findDifference(datesBetween, datesAvailable);

    }

    public List<HistoricDataDto> getAllEntries(){
        List<HistoricData> allData = repository.findByOrderByDateImportedDescDataDateAsc();
        List<HistoricDataDto> returnDtoList = new ArrayList<>();
        for(int i = 0; i<allData.size(); i++){
            returnDtoList.add(mapToDto(allData.get(i)));
        }
        return returnDtoList;
    }

    // handling for edge cases
    // private void updateExpiry(LocalDateTime date, LocalDateTime newExpiry){
    //     HistoricData data = repository.findByDataDate(date);
    //     data.dateExpiry = newExpiry;
    //     repository.save(data);
    // }

    private static List<LocalDate> getDateRange(LocalDate start, LocalDate end) {

        List<LocalDate> ret = new ArrayList<LocalDate>();
        LocalDate tmp = start;
        while(tmp.isBefore(end) || tmp.equals(end)) {
            ret.add(tmp);
            tmp = tmp.plusDays(1);
        }
        return ret;
    }

    private static <T> Set<T> findDifference(List<T> first, List<T> second)
    {
        Set<T> diff = new HashSet<>(first);
        diff.removeAll(second);
        return diff;
    }

    private HistoricDataDto mapToDto(@NonNull final HistoricData entity){
        return HistoricDataDto.builder().
            id(entity.getId()).
            userId(entity.getUserId()).
            dataDate(entity.getDataDate()).
            dateImported(entity.getDateImported()).
            dateExpiry(entity.getDateExpiry()).
            expired(entity.isExpired()).
            source(entity.getSource()).
            build();
    }

    private HistoricData mapToEntity(@NonNull final HistoricDataDto dto){
        return HistoricData.builder().
            id(dto.getId()).
            userId(dto.getUserId()).
            dataDate(dto.getDataDate()).
            dateImported(dto.getDateImported()).
            dateExpiry(dto.getDateExpiry()).
            expired(dto.isExpired()).
            source(dto.getSource()).
            build();
    }
}
