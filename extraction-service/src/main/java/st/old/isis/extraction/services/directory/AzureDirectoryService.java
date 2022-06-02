package st.old.isis.extraction.services.directory;

import com.azure.core.util.Context;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.extraction.utils.DownloadUtils;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class AzureDirectoryService {

  private final ShareDirectoryClient rootDirectory;
  @Autowired private DownloadUtils downloadUtils;

  protected abstract String source();

  public void deleteDirectory(String directoryPrefix, String fileToDelete) {
    final var options = new ShareListFilesAndDirectoriesOptions();

    options.setPrefix(directoryPrefix);
    options.setMaxResultsPerPage(10);

    final var timeout = Duration.ofSeconds(downloadUtils.getTimeout());

    final var source = source();

    final var items =
        rootDirectory.listFilesAndDirectories(options, timeout, new Context(source, source));

    if (items.stream().findAny().isEmpty()) {
      log.info("Cannot find any items on directory: {}", source);
    }

    items.forEach(item -> inspectItem(rootDirectory, item, fileToDelete));
  }

  private void inspectItem(
      @NonNull final ShareDirectoryClient directory,
      @NonNull final ShareFileItem item,
      @NonNull final String fileToDelete) {

    if (item.isDirectory()) {
      log.info("Directory: {}", item.getName());
      if (item.getName().equalsIgnoreCase(fileToDelete)) {
        final var subdirectory = directory.getSubdirectoryClient(item.getName());
        final var timeout = Duration.ofSeconds(downloadUtils.getTimeout());
        subdirectory
            .listFilesAndDirectories()
            .forEach(
                i -> {
                  log.info("Deleting file: {}", i.getName());
                  subdirectory.deleteFileWithResponse(
                      i.getName(), timeout, new Context(item.getName(), item.getName()));
                });
        log.info("Deleting directory: {}", item.getName());
        subdirectory.delete();
      } else {
        enterSubdirectory(directory, item.getName(), fileToDelete);
      }
    } else {
      if (item.getName().equalsIgnoreCase(fileToDelete)) {
        log.info("Deleting file: {}", item.getName());
        directory.deleteFile(item.getName());
      } else {
        log.info("Ignore item: {}", item.getName());
      }
    }
  }

  private void enterSubdirectory(
      @NonNull final ShareDirectoryClient client,
      @NonNull final String directory,
      String fileToDelete) {

    log.info("Entering directory: {}", directory);

    final var subdirectory = client.getSubdirectoryClient(directory);

    final var items = subdirectory.listFilesAndDirectories();

    if (items.stream().findAny().isEmpty()) {
      log.info("Cannot find any items on directory: {}", directory);
    }

    items.forEach(item -> inspectItem(subdirectory, item, fileToDelete));
  }
}
