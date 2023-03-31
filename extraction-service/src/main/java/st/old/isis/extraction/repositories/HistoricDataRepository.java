package st.old.isis.extraction.repositories;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import st.old.isis.extraction.entities.HistoricData;

public interface HistoricDataRepository extends JpaRepository<HistoricData, Integer>, JpaSpecificationExecutor <HistoricData>{
    List<HistoricData> findByDataDate(LocalDate date);

    List<HistoricData> findByDataDateBetween(LocalDate dateTo, LocalDate dateFrom);

    List<HistoricData> findBySource(String source);

    List<HistoricData> findByExpired(boolean expiry);

    List<HistoricData> findByOrderByDateImportedDescDataDateAsc();

}
