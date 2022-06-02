package st.old.isis.extraction.context;

import st.old.isis.enums.BatchResultType;
import st.old.isis.utils.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static st.old.isis.enums.BatchResultType.COMPLETED;

public class DownloadContext {

  private static final ThreadLocal<Map<BatchResultType, List<String>>> BATCH_FILES_MAP =
      ThreadLocal.withInitial(HashMap::new);

  public static Map<BatchResultType, List<String>> getBatchFilesMap() {
    return BATCH_FILES_MAP.get();
  }

  private static List<String> getBatchFiles(BatchResultType type) {

    final var files = getBatchFilesMap().get(type);

    if (ListUtils.isNullOrEmpty(files)) {
      BATCH_FILES_MAP.get().put(type, new ArrayList<>());
    }

    return getBatchFilesMap().get(type);
  }

  public static void addCompletedFile(BatchResultType type, String fileSource) {
    if (!getBatchFiles(type).contains(fileSource)) {
      getBatchFiles(type).add(fileSource);
    }
  }

  public static void addCompletedFile(String fileSource) {
    if (!getBatchFiles(COMPLETED).contains(fileSource)) {
      getBatchFiles(COMPLETED).add(fileSource);
    }
  }

  public static void addFailedFile(String fileSource) {
    getBatchFiles(BatchResultType.FAILED).add(fileSource);
  }
}
