package st.old.isis.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Dto {

  private static final long serialVersionUID = -6775035042277906787L;

  private String subject;

  private String body;

  private Map<String, String> data;
}
