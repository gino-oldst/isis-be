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
public class KeywordDto implements Dto {

    private static final long serialVersionUID = -698572348489590869L;

    private String id;

    private String word;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;
}
