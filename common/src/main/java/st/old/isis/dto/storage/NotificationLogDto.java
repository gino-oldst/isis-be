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
public class NotificationLogDto implements Dto {

    private static final long serialVersionUID = 5779367634794321550L;

    private String id;

    private String userId;

    private String message;

    private boolean seen;

    private LocalDateTime dateCreated;

    private boolean deleted;
}
