package st.old.isis.extraction.repositories;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import st.old.isis.extraction.entities.HistoricData;

public interface HistoricDataRepository extends JpaRepository<HistoricData, Integer>, JpaSpecificationExecutor <HistoricData>{
    HistoricData findByDataDate(LocalDate date);

    List<HistoricData> findAllDataDateBetween(LocalDate dateFrom, LocalDate dateTo);
}
