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
        allDates.removeAll(missingDates);
        if (allDates.size() > 0) return false;

        return true;
    }

    public HistoricData createEntity(@NonNull @Audited final HistoricDataDto dto){
        final var entity = mapToEntity(dto);
        final var createdEntity = repository.save(entity);

        return createdEntity;
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
                                historicDataStatus("Downloading").
                                build();

        HistoricData entity = createEntity(dto);
        return mapToDto(entity);
    }

    public List<HistoricDataDto> getAllUnexpiredDates(){
        List<HistoricData> unexpired = repository.findByExpired(false);

        List<HistoricDataDto> returnDtoList = new ArrayList<>();
        for(int i = 0; i<unexpired.size(); i++){
            returnDtoList.add(mapToDto(unexpired.get(i)));
        }

        return returnDtoList;
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
                                    historicDataStatus("Downloading").
                                    build();
            HistoricData entity = createEntity(dto);
            returnDto.add(mapToDto(entity));
        }

        return returnDto;
    }

    public Set<LocalDate> missingDates(LocalDate dateFrom, LocalDate dateTo, String source){
        List<LocalDate> datesBetween = getDateRange(dateFrom, dateTo);
        List<HistoricData> availableData = repository.findBySourceAndExpiredAndDataDateBetween(source, false, dateFrom, dateTo);
//        System.out.println(repository.findBySourceAndExpiredAndDataDateBetween(source, false, dateFrom, dateTo));
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
            historicDataStatus(entity.getHistoricDataStatus()).
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
            historicDataStatus(dto.getHistoricDataStatus()).
            build();
    }

    public HistoricDataDto update(@NonNull HistoricDataDto dto){
        HistoricData data = findById(dto.getId());

        data.setHistoricDataStatus(dto.getHistoricDataStatus());
        data.setExpired(dto.isExpired());
        data.setDateExpiry(dto.getDateExpiry());

        repository.save(data);

        System.out.println(data);

        return mapToDto(data);

    }

    public HistoricDataDto setStatus(@NonNull HistoricDataDto dto, String status){
        HistoricData data = findById(dto.getId());

        System.out.println(data);

        data.setHistoricDataStatus(status);

        repository.save(data);

        System.out.println(data);

        return mapToDto(data);
    }

    private HistoricData findById(@NonNull String id) {
        return repository.findById(id);
    }

    public void bulkDeleteStatus(LocalDate date){
        List<HistoricData> dataList = repository.findByDataDate(date);
        for(int i = 0; i < dataList.size(); i++){
            System.out.println(dataList.get(i));
            dataList.get(i).setExpired(true);
            dataList.get(i).setHistoricDataStatus("Deleted");
            System.out.println(dataList.get(i));
            repository.save(dataList.get(i));
        }
    }


}
