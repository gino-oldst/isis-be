package st.old.isis.storage.repositories.temp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import st.old.isis.storage.entities.temp.TempCommunicationRecipient;

public interface TempCommunicationRecipientRepository
    extends JpaRepository<TempCommunicationRecipient, String> {

  @Query(
      value =
          "DELETE from temp_communication_recipient WHERE file_source = ?1 SELECT @@ROWCOUNT AS mergedItems",
      nativeQuery = true)
  int deleteAllByFileSource(@NonNull final String fileSource);
}
