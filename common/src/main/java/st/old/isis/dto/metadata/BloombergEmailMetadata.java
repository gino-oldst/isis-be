package st.old.isis.dto.metadata;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BloombergEmailMetadata extends Metadata {

  private static final long serialVersionUID = -9193581254799907386L;

  private List<BloombergAttachment> attachments;
}
