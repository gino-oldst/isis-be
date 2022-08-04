package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.storage.entities.DataImportRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DataImportRecordRepository extends JpaRepository<DataImportRecord, String> {

    List<DataImportRecord> findByProcessedStatusOrderByImportDateAsc(ProcessedStatus processedStatus);

    Optional<DataImportRecord> findBySourceAndImportDate(String source, LocalDate importDate);

    List<DataImportRecord> findByProcessedStatusAndSourceOrderByImportDateAsc(ProcessedStatus processedStatus, String source);

}
