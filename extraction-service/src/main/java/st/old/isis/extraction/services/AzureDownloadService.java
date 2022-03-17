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
import org.springframework.stereotype.Service;
import st.old.isis.dto.notification.MessageDto;
import st.old.isis.dto.notification.NotificationRequestDto;
import st.old.isis.extraction.entity.DownloadVersionControl;
import st.old.isis.extraction.entity.FileDownloadHistory;
import st.old.isis.extraction.services.internal.NotificationService;
import st.old.isis.extraction.utils.DownloadUtils;
import st.old.isis.utils.AuditUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

  protected abstract String source();

  protected abstract boolean skipFile(@NonNull final String filename);

  protected abstract void processFile(@NonNull final ShareFileClient client);

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

      items.forEach(item -> inspectItem(rootDirectory, item, source));

    } catch (Throwable err) {
      sendDownloadingError(err);
      throw err;
    }
  }

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

      items.forEach(item -> inspectItem(rootDirectory, item, source));

    } catch (Throwable err) {
      sendDownloadingError(err);
      throw err;
    }
  }

  private String getPrefix(@NonNull final String date) {

    final var prefixFormatter = DateTimeFormatter.ofPattern("yyyy-MM-");

    final var now = LocalDate.now();

    if (now.lengthOfMonth() == now.getDayOfMonth()) {
      return now.format(prefixFormatter);
    }

    final var lastFileDate = LocalDate.parse(date, DownloadUtils.FILENAME_FORMAT);

    final var lastDateSuffix = lastFileDate.getDayOfMonth() / 10;
    final var currentDateSuffix = now.getDayOfMonth() / 10;

    if (lastDateSuffix == currentDateSuffix) {
      return lastFileDate.format(prefixFormatter) + currentDateSuffix;
    } else {
      return now.format(prefixFormatter);
    }
  }

  private void inspectItem(
      @NonNull final ShareDirectoryClient directory,
      @NonNull final ShareFileItem item,
      @NonNull final String directoryName) {

    if (item.isDirectory()) {
      enterSubdirectory(directory, item.getName());
    } else {
      processFile(directory, item, directoryName);
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

    items.forEach(item -> inspectItem(subdirectory, item, directory));
  }

  private void processFile(
      @NonNull final ShareDirectoryClient client,
      @NonNull final ShareFileItem item,
      @NonNull final String directoryName) {

    final var filename = item.getName();

    if (skipFile(filename)) {
      log.info("File ignored: {}", filename);
      return;
    }

    final var sha1 = getSha1String(client, filename);

    if (skipFileBasedOnSha1(sha1, filename)) {
      log.info("File ignored, already processed: {}", filename);
      return;
    }

    log.info("Processing file: {}", filename);

    final var fileClient = client.getFileClient(filename);

    processFile(fileClient);

    saveToHistory(sha1, fileClient.getFilePath());
    saveToVersionControl(directoryName);
  }

  private String getSha1String(
      @NonNull final ShareDirectoryClient client, @NonNull final String filename) {

    final var sha1File = filename + ".sha1";

    final var sha1InputStream = client.getFileClient(sha1File).openInputStream();

    return readSha1(sha1InputStream).trim();
  }

  private boolean skipFileBasedOnSha1(@NonNull final String sha1, @NonNull final String filename) {

    if (Strings.isEmpty(sha1)) {
      log.info("Skipping file Sha1 is empty for file name: [{}]", filename);
      return true;
    }

    final var isSha1Existing = historyService.isExisting(sha1);

    if (isSha1Existing) {
      log.info("Skipping file Sha1 is existing: [{}]", sha1);
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

  private void saveToHistory(@NonNull final String sha1, @NonNull final String filePath) {

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
