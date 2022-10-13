package st.old.isis.storage.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.EscalationHistoryDto;
import st.old.isis.exception.IsisException;


@Component
@Slf4j
public class EscalationHistoryValidator {

    public void validate(EscalationHistoryDto dto) throws IsisException {

        if (dto.getAlertId() == null){
            throw new IsisException("Invalid Alert Id "+ dto.getAlertId(), "InvalidEscalationHistoryDataException");
        }

        if (dto.getEscalatedFrom() == null){
            throw new IsisException("Invalid Escalation From "+ dto.getEscalatedFrom(), "InvalidEscalationHistoryDataException");
        }

        if (dto.getEscalatedTo() == null){
            throw new IsisException("Invalid Escalation To "+ dto.getEscalatedTo(), "InvalidEscalationHistoryDataException");
        }

        if (dto.getExecutorId() == null){
            throw new IsisException("Invalid Executor Id "+ dto.getExecutorId(), "InvalidEscalationHistoryDataException");
        }

    }
}
