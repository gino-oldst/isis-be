package st.old.isis.dto.metadata;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BloombergChatAttachmentMetadata extends Metadata {

  private static final long serialVersionUID = -4116721414207884346L;

  private BloombergAttachment attachment;
}
