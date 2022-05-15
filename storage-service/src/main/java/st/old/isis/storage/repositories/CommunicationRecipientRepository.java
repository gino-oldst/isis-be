package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import st.old.isis.storage.entities.CommunicationRecipient;

public interface CommunicationRecipientRepository
    extends JpaRepository<CommunicationRecipient, String>,
        JpaSpecificationExecutor<CommunicationRecipient> {

  @Query(
      value =
          "\n"
              + "MERGE communication_recipient AS main\n"
              + "USING (\n"
              + "\tSELECT *\n"
              + "    FROM (\n"
              + "        SELECT *, ROW_NUMBER() OVER (PARTITION BY recipient, chain_link_id ORDER BY date_imported DESC) AS rowNum\n"
              + "        FROM temp_communication_recipient\n"
              + "    ) t\n"
              + "    WHERE t.rowNum = 1) AS temp ON (main.recipient = temp.recipient and main.chain_link_id = temp.chain_link_id)\n"
              + "WHEN NOT MATCHED BY TARGET\n"
              + "THEN \n"
              + "INSERT (id, chain_link_id, date_imported, metadata, recipient)\n"
              + "VALUES (temp.id, temp.chain_link_id, temp.date_imported, temp.metadata, temp.recipient); SELECT @@ROWCOUNT AS mergedItems",
      nativeQuery = true)
  int merge();
}
