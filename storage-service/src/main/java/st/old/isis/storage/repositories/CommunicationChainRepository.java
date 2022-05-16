package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import st.old.isis.storage.entities.CommunicationChain;

public interface CommunicationChainRepository
    extends JpaRepository<CommunicationChain, String>,
        JpaSpecificationExecutor<CommunicationChain> {

  @Query(
      value =
          "MERGE communication_chain AS main\n"
              + "USING (\n"
              + "\tSELECT *\n"
              + "    FROM (\n"
              + "        SELECT *, ROW_NUMBER() OVER (PARTITION BY source_chain_id, source ORDER BY date_modified DESC) AS rowNum\n"
              + "        FROM temp_communication_chain where file_source = ?1\n"
              + "    ) t\n"
              + "    WHERE t.rowNum = 1) AS temp ON (main.source_chain_id = temp.source_chain_id and main.source = temp.source)\n"
              + "WHEN NOT MATCHED BY TARGET\n"
              + "THEN \n"
              + "INSERT (id, date_created, date_from, date_imported, date_modified, date_period, date_to, deleted, source, source_chain_id, subject)\n"
              + "VALUES (temp.id, temp.date_created, temp.date_from, temp.date_imported, temp.date_modified, temp.date_period, temp.date_to, temp.deleted, temp.source, temp.source_chain_id, temp.subject); SELECT @@ROWCOUNT AS mergedItems",
      nativeQuery = true)
  int merge(String fileSource);
}
