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

    public boolean allDatesMissing(LocalDate dateFrom, LocalDate dateTo){
        Set<LocalDate> missingDates = missingDates(dateFrom, dateTo);
        List<LocalDate> datesBetween = getDateRange(dateFrom, dateTo);
        Set<LocalDate> allDates = new HashSet<>(datesBetween);
        missingDates.removeAll(allDates);
        if (missingDates.size() > 0) return false;

        return true;
    }

    public HistoricDataDto create(@NonNull @Audited final HistoricDataDto dto){
        final var entity = mapToEntity(dto);
        final var createdEntity = repository.save(entity);

        return mapToDto(createdEntity);
    }

    public List<HistoricDataDto> bulkCreate(LocalDate dateFrom, LocalDate dateTo, String userId, String source){
        List<LocalDate> allDates = getDateRange(dateFrom, dateTo);
        List<HistoricDataDto> returnDto = new ArrayList<HistoricDataDto>();
        for(int i = 0; i < allDates.size(); i++){
            HistoricDataDto dto = HistoricDataDto.builder().
                                    userId(userId).
                                    dataDate(allDates.get(i)).
                                    dateExpiry(allDates.get(i).plusMonths(1)).
                                    dateImported(LocalDate.now()).
                                    expired(false).
                                    source(source).
                                    build();
            returnDto.add(create(dto));
        }

        return returnDto;
    }

    public Set<LocalDate> missingDates(LocalDate dateFrom, LocalDate dateTo){
        List<LocalDate> datesBetween = getDateRange(dateFrom, dateTo);
        List<HistoricData> availableData = repository.findByDataDateBetween(dateTo, dateFrom);
        List<LocalDate> datesAvailable = new ArrayList<LocalDate>();
        for(int i = 0; i<availableData.size(); i++){
            HistoricDataDto dataDto = mapToDto(availableData.get(i));
            datesAvailable.add(dataDto.getDataDate());
        }

        return findDifference(datesAvailable, datesBetween);

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
            dateExpiry(entity.getDataDate()).
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
            dateExpiry(dto.getDataDate()).
            expired(dto.isExpired()).
            source(dto.getSource()).
            build();
    }
}
