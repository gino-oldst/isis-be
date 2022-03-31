package st.old.isis.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.Dto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentMessageDto implements Dto {

    private static final long serialVersionUID = -5790958047123155040L;

    private CommunicationMessageDto message;
    private List<CommunicationMessageDto> before;
    private List<CommunicationMessageDto> after;
}
