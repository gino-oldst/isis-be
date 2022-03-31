package st.old.isis.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloombergAttachment implements Serializable {

  private static final long serialVersionUID = 6508838075515465536L;

  private String filename;
  private String fileId;
  private String fileSize;
}
