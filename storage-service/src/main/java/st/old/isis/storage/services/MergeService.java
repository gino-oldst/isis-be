package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.enums.BatchResultType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MergeService {

  private final ExternalUserService externalUserService;
  private final CommunicationChainService communicationChainService;
  private final CommunicationMessageService communicationMessageService;
  private final CommunicationRecipientService communicationRecipientService;

  @Audited(event = "MERGING_TEMP_FILES")
  public void merge(@Audited @NonNull final Map<BatchResultType, List<String>> files) {
    log.info("Merging all processed files...");
    final long start = System.currentTimeMillis();
    files.entrySet().forEach(this::merge);
    log.info("Merge files time elapsed: {} ms", System.currentTimeMillis() - start);
  }

  private void merge(Map.Entry<BatchResultType, List<String>> entry) {

    final var fileSources = entry.getValue();

    if (BatchResultType.COMPLETED == entry.getKey()) {
      log.info("Merging external users of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(externalUserService::finalizeBatch);

      log.info("Merging chains of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(communicationChainService::finalizeBatch);

      log.info("Merging messages of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(communicationMessageService::finalizeBatch);

      log.info("Merging recipients of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(communicationRecipientService::finalizeBatch);
    }

    if (BatchResultType.FAILED == entry.getKey()) {
      log.info(
          "Deleting temp external users of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(externalUserService::deleteTempExternalUser);

      log.info("Deleting temp chains of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(communicationChainService::deleteTempChains);

      log.info("Deleting temp messages of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(communicationMessageService::deleteTempMessages);

      log.info("Deleting temp recipients of files [{}]", Arrays.toString(fileSources.toArray()));
      fileSources.forEach(communicationRecipientService::deleteTempRecipients);
    }
  }
}
