package st.old.isis.extraction.services;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.extraction.entities.DownloadVersionControl;
import st.old.isis.extraction.entities.FileDownloadHistory;
import st.old.isis.extraction.services.internal.AuditService;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.utils.DownloadUtils;
import st.old.isis.utils.AuditUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class AzureDownloadService {

  private final ShareDirectoryClient rootDirectory;
  @Autowired private FileDownloadHistoryService historyService;
  @Autowired private DownloadVersionControlService versionControlService;
  @Autowired private DownloadUtils downloadUtils;
  @Autowired private NotificationService notificationService;
  @Autowired protected AuditUtil auditUtil;
  @Autowired protected AuditService auditService;

  protected abstract String source();

  protected abstract String getPrefix(@NonNull final String prefix);

  protected abstract boolean skipFile(@NonNull final String filename);

  protected abstract void processFile(
      @NonNull final ShareFileClient client,
      @NonNull final String filename,
      @NonNull final String fileId);

  @Async
  public void sync() {

    try {

      final var source = source();

      log.info("Checking [{}] directory", source);

      final var optVersion = versionControlService.getVersionControl(source);

      final PagedIterable<ShareFileItem> items;

      if (optVersion.isPresent()) {

        final var version = optVersion.get();
        final var prefix = getPrefix(version.getLastFileName());

        log.info("Getting files with prefix: {}", prefix);

        final var options = new ShareListFilesAndDirectoriesOptions();

        options.setPrefix(prefix);
        options.setMaxResultsPerPage(10);

        final var timeout = Duration.ofSeconds(downloadUtils.getTimeout());

        items =
            rootDirectory.listFilesAndDirectories(options, timeout, new Context(source, source));

      } else {

        items = rootDirectory.listFilesAndDirectories();
      }

      if (items.stream().findAny().isEmpty()) {
        log.info("Cannot find any items on directory: {}", source);
      }

      items.forEach(item -> inspectItem(rootDirectory, item));

    } catch (Throwable err) {
      sendDownloadingError(err);
      throw err;
    }
  }

  @Async
  public void sync(@NonNull final String prefix) {

    try {

      final var source = source();
      final var options = new ShareListFilesAndDirectoriesOptions();

      options.setPrefix(prefix);
      options.setMaxResultsPerPage(10);

      final var timeout = Duration.ofSeconds(downloadUtils.getTimeout());

      final var items =
          rootDirectory.listFilesAndDirectories(options, timeout, new Context(source, source));

      if (items.stream().findAny().isEmpty()) {
        log.info("Cannot find any items on directory: {}", source);
      }

      items.forEach(item -> inspectItem(rootDirectory, item));

    } catch (Throwable err) {
      sendDownloadingError(err);
      throw err;
    }
  }

  private void inspectItem(
      @NonNull final ShareDirectoryClient directory, @NonNull final ShareFileItem item) {

    if (item.isDirectory()) {
      enterSubdirectory(directory, item.getName());
    } else {
      processFile(directory, item);
    }
  }

  private void enterSubdirectory(
      @NonNull final ShareDirectoryClient client, @NonNull final String directory) {

    log.info("Entering directory: {}", directory);

    final var subdirectory = client.getSubdirectoryClient(directory);

    final var items = subdirectory.listFilesAndDirectories();

    if (items.stream().findAny().isEmpty()) {
      log.info("Cannot find any items on directory: {}", directory);
    }

    items.forEach(item -> inspectItem(subdirectory, item));
    saveToVersionControl(directory);
  }

  private void processFile(
      @NonNull final ShareDirectoryClient client, @NonNull final ShareFileItem item) {

    final var filename = item.getName();

    if (skipFile(filename)) {
      log.info("File ignored: {}", filename);
      return;
    }

    final var fileId = getFileId(client, filename);

    if (skipFileBasedOnFileId(fileId, filename)) {
      log.info("File ignored, already processed: {}", filename);
      return;
    }

    final long start = System.currentTimeMillis();

    log.info("Processing file: {}", filename);

    final var fileClient = client.getFileClient(filename);

    processFile(fileClient, filename, fileId);

    log.info("Elapsed " + (System.currentTimeMillis() - start) + " ms");
  }

  private String getFileId(
      @NonNull final ShareDirectoryClient client, @NonNull final String filename) {

    final var sha1File = client.getFileClient(filename + ".sha1");

    final var sha1Exists = sha1File.exists();

    if (!sha1Exists) {
      return filename;
    }

    final var sha1InputStream = sha1File.openInputStream();

    return readSha1(sha1InputStream).trim();
  }

  private boolean skipFileBasedOnFileId(
      @NonNull final String fileId, @NonNull final String filename) {

    if (Strings.isEmpty(fileId)) {
      log.info("Skipping file id is empty for file name: [{}]", filename);
      return true;
    }

    final var isFileIdExisting = historyService.isExisting(fileId);

    if (isFileIdExisting) {
      log.info("Skipping file id is existing: [{}]", fileId);
      return true;
    }

    return false;
  }

  private String readSha1(@NonNull final InputStream inputStream) {

    final var resultStringBuilder = new StringBuilder();
    final var inputStreamReader = new InputStreamReader(inputStream);

    try (final var br = new BufferedReader(inputStreamReader)) {

      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return resultStringBuilder.toString();
  }

  protected void saveToHistory(@NonNull final String sha1, @NonNull final String filePath) {

    final var history = FileDownloadHistory.builder().id(sha1).filePath(filePath).build();

    historyService.saveHistory(history);
  }

  private void saveToVersionControl(@NonNull final String filename) {

    final var version =
        DownloadVersionControl.builder().source(source()).lastFileName(filename).build();

    versionControlService.saveVersionControl(version);
  }

  private void sendDownloadingError(Throwable err) {
    log.error(String.format("An error occurred when downloading %s raw file", source()), err);

    final var request =
        NotificationRequestDto.builder()
            .customMessage(
                MessageDto.builder()
                    .subject("Download Failed")
                    .body(
                        String.format(
                            "Download %s raw date file failed: %s", source(), err.getMessage()))
                    .build())
            // .recipients() todo: recipients
            .build();

    notificationService.sendNotification(List.of(request));
  }
}
