package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLinkDto implements Dto {

    private static final long serialVersionUID = 4878972056975832116L;

    private String id;

    private String userId;

    private String externalUserId;

    private LocalDateTime dateCreated;

    private boolean deleted;
}
