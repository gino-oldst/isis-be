package st.old.isis.storage.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import st.old.isis.storage.entities.CommunicationMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CommunicationMessageRepository
    extends JpaRepository<CommunicationMessage, String>,
        JpaSpecificationExecutor<CommunicationMessage> {

  @Query(
      value =
          "MERGE communication_message AS main\n"
              + "USING (\n"
              + "\tSELECT *\n"
              + "    FROM (\n"
              + "        SELECT *, ROW_NUMBER() OVER (PARTITION BY source, sender, chain_link_id, time_sent ORDER BY date_modified DESC) AS rowNum\n"
              + "        FROM temp_communication_message where file_source = ?1\n"
              + "    ) t\n"
              + "    WHERE t.rowNum = 1) AS temp ON (main.source = temp.source and main.sender = temp.sender and main.chain_link_id = temp.chain_link_id and main.time_sent = temp.time_sent)\n"
              + "WHEN NOT MATCHED BY TARGET\n"
              + "THEN \n"
              + "INSERT (id, chain_link_id, date_created, date_modified, deleted, import_date, message, metadata, sender, source, source_message_id, subject, time_sent, type, bypass_type)\n"
              + "VALUES (temp.id, temp.chain_link_id, temp.date_created, temp.date_modified, temp.deleted, temp.import_date, temp.message, temp.metadata, temp.sender, temp.source, temp.source_message_id, temp.subject, temp.time_sent, temp.type, temp.bypass_type); SELECT @@ROWCOUNT AS mergedItems",
      nativeQuery = true)
  int merge(String fileSource);


  Page<CommunicationMessage> findAllBySourceAndImportDateBetween(String source, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

}
