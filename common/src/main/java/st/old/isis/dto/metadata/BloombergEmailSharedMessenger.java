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
public class BloombergEmailSharedMessenger implements Serializable {

  private static final long serialVersionUID = -8334250759913450788L;

  private String sender;
}
