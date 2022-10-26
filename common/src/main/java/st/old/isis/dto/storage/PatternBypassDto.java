package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;
import st.old.isis.enums.MessageBypassType;
import st.old.isis.enums.PatternType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternBypassDto implements Dto {

    private static final long serialVersionUID = 3162572894372987299L;

    private String id;

    private String value;

    private PatternType patternType;

    private MessageBypassType bypassType;

    private String targetMessageTypes;

    private LocalDateTime dateCreated;

    private LocalDateTime dateModified;

    private String responseStatus;

    private String exception;

    private String exceptionMessage;

    private boolean deleted;
}
