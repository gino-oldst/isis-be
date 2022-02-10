package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.CommunicationLogDto;
import st.old.isis.exception.IsisException;

@Component
@RequiredArgsConstructor
public class CommunicationLogValidator {

    public void validate(@NonNull final CommunicationLogDto dto) throws IsisException {
        //todo: add validations for communication log
    }
}
