package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.NotificationLogDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.NotificationLog;

@Component
@RequiredArgsConstructor
public class NotificationLogValidator {

    public void validate(@NonNull final NotificationLogDto dto) throws IsisException {
        //todo: add validations for notification log
    }

    public void updateValidate(@NonNull final NotificationLog oldEntity,
                               @NonNull final NotificationLogDto dto) throws IsisException {

    }
}
