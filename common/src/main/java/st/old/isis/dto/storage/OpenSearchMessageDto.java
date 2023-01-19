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
public class OpenSearchMessageDto implements Dto {

    private static final long serialVersionUID = 7222918429149225853L;

    private String id;

    private String chain_id;

    private String time_sent;

    private String sender;

    private String recipients;

    private String source;

    private String type;

    private String content;

    private String import_date;
}