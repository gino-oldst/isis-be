package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.storage.entities.DataImportRecord;

import java.util.List;

public interface DataImportRecordRepository extends JpaRepository<DataImportRecord, String> {

    List<DataImportRecord> findByProcessedStatusOrderByImportDateAsc(ProcessedStatus processedStatus);

    List<DataImportRecord> findByProcessedStatusAndSourceOrderByImportDateAsc(ProcessedStatus processedStatus, String source);

}
