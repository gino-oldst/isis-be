package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AlertLog;

@Component
@RequiredArgsConstructor
public class AlertLogValidator {

    public void validate(@NonNull final AlertLogDto dto) throws IsisException {
        //todo: add validations for communication log
    }

    public void updateValidate(@NonNull final AlertLog oldEntity,
                               @NonNull final AlertLogDto dto) throws IsisException {

    }
}
