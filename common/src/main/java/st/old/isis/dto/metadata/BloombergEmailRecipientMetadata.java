package st.old.isis.dto.metadata;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BloombergEmailRecipientMetadata extends Metadata {

    private static final long serialVersionUID = 8217319759052108741L;

    private String forwardedBy;
    private String forwardedTo;
}
