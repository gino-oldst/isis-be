package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationRecipientDto implements Dto {

    private static final long serialVersionUID = 8815849146610375028L;

    private String id;

    private String commLogId;

    private String recipient;
}
