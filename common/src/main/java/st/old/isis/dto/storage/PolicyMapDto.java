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
public class PolicyMapDto implements Dto {

    private static final long serialVersionUID = -6400089411488317737L;

    private String id;

    private String keywordId;

    private String policyId;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private boolean deleted;
}
