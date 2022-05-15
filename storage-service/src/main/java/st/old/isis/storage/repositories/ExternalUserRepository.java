package st.old.isis.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.ExternalUser;

import java.util.Optional;

public interface ExternalUserRepository
    extends JpaRepository<ExternalUser, String>, JpaSpecificationExecutor<ExternalUser> {

  Optional<ExternalUser> findByExternalId(@NonNull final String externalId);

  @Query(
      value =
          "MERGE external_user AS main\n"
              + "USING (\n"
              + "\tSELECT *\n"
              + "    FROM (\n"
              + "        SELECT *, ROW_NUMBER() OVER (PARTITION BY external_id, source ORDER BY date_modified DESC) AS rowNum\n"
              + "        FROM temp_external_user\n"
              + "    ) t\n"
              + "    WHERE t.rowNum = 1) AS temp ON (main.external_id = temp.external_id and main.source = temp.source)\n"
              + "WHEN NOT MATCHED BY TARGET\n"
              + "THEN \n"
              + "INSERT (id, date_created, date_modified, deleted, email, external_id, first_name, last_name, phone_number, source, type)\n"
              + "VALUES (temp.id, temp.date_created, temp.date_modified, temp.deleted, temp.email, temp.external_id, temp.first_name, temp.last_name, temp.phone_number, temp.source, temp.type); SELECT @@ROWCOUNT AS mergedItems",
      nativeQuery = true)
  int merge();
}
