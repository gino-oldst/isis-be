package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.AlertStatusType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    private String alertId;

    private AlertStatusType alertStatus;

    private boolean seen;

    private LocalDateTime dateCreated;

    private boolean deleted;
}
