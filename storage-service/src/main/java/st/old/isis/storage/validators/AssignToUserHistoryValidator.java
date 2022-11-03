package st.old.isis.storage.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.AssignToUserHistoryDto;
import st.old.isis.exception.IsisException;


@Component
@Slf4j
public class AssignToUserHistoryValidator {

    public void validate(AssignToUserHistoryDto dto) throws IsisException {

        if (dto.getAlertId() == null){
            throw new IsisException("Invalid Alert Id "+ dto.getAlertId(), "InvalidAssignedToUserHistoryDataException");
        }

        if (dto.getAssignedFrom() == null){
            throw new IsisException("Invalid Escalation From "+ dto.getAssignedFrom(), "InvalidAssignedToUserHistoryDataException");
        }
    }
}
