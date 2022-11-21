package st.old.isis.dto.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.Source;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLinkIdentifierDto implements Dto {

    private static final long serialVersionUID = 4855687143486344722L;

    private String id;

    private String userLinkId;

    private String identifier;

    @JsonIgnoreProperties
    private Source source;

    private String externalUserId;

    @JsonIgnoreProperties
    private String identifierType;

    @JsonIgnoreProperties
    private LocalDate importDate;

    @JsonIgnoreProperties
    private LocalDateTime dateCreated;

    @JsonIgnoreProperties
    private LocalDateTime dateModified;

    @JsonIgnoreProperties
    private boolean deleted;

}
