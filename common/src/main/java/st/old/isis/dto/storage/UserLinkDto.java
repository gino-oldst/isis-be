package st.old.isis.dto.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLinkDto implements Dto {

    private static final long serialVersionUID = 4878972056975832116L;

    private String id;

    private String userId;

    private String externalUserId;

    private String firstName;

    private String lastName;

    private String company;

    private String externalCompanyId;

    private boolean isInternal;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private List<String> identifiers;

    private List<UserLinkIdentifierDto> userLinkIdentifierDtos;

    private boolean deleted;

    private String likeFirstName;

    private String likeLastName;

}
