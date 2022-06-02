package st.old.isis.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import st.old.isis.dto.CsvList;
import st.old.isis.dto.Dto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CsvUtil {

  public static void export(
      HttpServletResponse response,
      String filename,
      List<String> headers,
      List<? extends Dto> object) {
    try {
      response.setContentType("text/csv");
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setHeader("Content-Disposition", String.format("attachment; filename=%s", filename));
      try (Writer writer =
          new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

        writer.write(String.join(",", headers).concat("\n"));

        for (Dto obj : object) {
          writer.write(obj.toCsvString());
        }
        writer.flush();
      }
    } catch (IOException e) {
      log.error("Failed to export csv file: {}", e.getMessage(), e);
    }
  }

  public static void exportList(
      HttpServletResponse response,
      String filename,
      List<String> headers,
      List<? extends CsvList> object) {
    try {
      response.setContentType("text/csv");
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setHeader("Content-Disposition", String.format("attachment; filename=%s", filename));
      try (Writer writer =
          new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

        writer.write(String.join(",", headers).concat("\n"));

        for (CsvList obj : object) {
          for (String csvString : obj.toCsvStringList()) {
            writer.write(csvString);
          }
        }
        writer.flush();
      }
    } catch (IOException e) {
      log.error("Failed to export csv file: {}", e.getMessage(), e);
    }
  }

  public static String mapToCsv(@NonNull final List<String> values) {
    return String.join(",", sanitizeList(values)).concat("\n");
  }

  private static String sanitizeString(@NonNull final String str) {
    return String.format("\"%s\"", str.trim());
  }

  private static List<String> sanitizeList(List<String> values) {
    return values.stream().map(CsvUtil::sanitizeString).collect(Collectors.toList());
  }
}
