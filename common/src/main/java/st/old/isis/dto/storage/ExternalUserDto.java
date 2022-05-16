package st.old.isis.dto.storage;

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
public class ExternalUserDto implements Dto {

  private static final long serialVersionUID = -8336343845400934731L;

  private String id;

  private String externalId;

  private String email;

  private String phoneNumber;

  private String firstName;

  private String lastName;

  private String type;

  private String source;

  private LocalDateTime dateCreated;

  private String fileSource;

  private boolean deleted;

  private List<String> ids;
  private List<String> externalIds;
  private List<String> sources;
  private String likeEmail;
  private String likeFirstName;
  private String likeLastName;
}
