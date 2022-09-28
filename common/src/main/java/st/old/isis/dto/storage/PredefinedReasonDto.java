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
public class PredefinedReasonDto implements Dto {

    private static final long serialVersionUID = 7509988287137563317L;

    private String id;

    private String value;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private String addedByAppUser;

    private boolean deleted;
}
