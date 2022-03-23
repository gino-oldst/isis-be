package st.old.isis.dto.list;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.exception.IsisException;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListDto<T extends Dto> implements Serializable {
  private static final long serialVersionUID = -3040663978030694562L;

  private long totalNoOfRecords;
  private long totalPages;
  private long totalCurrentElements;
  private List<T> data;
  private String responseStatus;
  private String exception;
  private String exceptionMessage;

  public static <T extends Dto> ListDto<T> exception(IsisException exception) {
    return ListDto.<T>builder()
        .responseStatus("error")
        .exception(exception.getException())
        .exceptionMessage(exception.getMessage())
        .build();
  }

  public static <T extends Dto> ListDto<T> emptyList() {
    return ListDto.<T>builder()
        .data(Collections.emptyList())
        .totalNoOfRecords(0)
        .totalPages(0)
        .totalCurrentElements(0)
        .responseStatus("success")
        .build();
  }

  @JsonIgnore
  public boolean isSuccess() {
    return "success".equals(this.responseStatus);
  }

  @JsonIgnore
  public boolean isError() {
    return "error".equals(this.responseStatus);
  }

  public ListDto<T> success() {
    responseStatus = "success";
    return this;
  }

  public ListDto<T> error() {
    responseStatus = "error";
    return this;
  }
}
