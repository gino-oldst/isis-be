package st.old.isis.dto.storage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
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

    private Long number;

    private String word;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime dateCreated;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime dateModified;

    private boolean deleted;
}
