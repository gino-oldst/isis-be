package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.CommunicationLogDto;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationRecipient;

@Component
@RequiredArgsConstructor
public class CommunicationRecipientValidator {

    public void validate(@NonNull final CommunicationLogDto dto) throws IsisException {

    }

    public void updateValidate(@NonNull final CommunicationRecipient oldEntity,
                               @NonNull final CommunicationRecipientDto dto) throws IsisException {

    }
}
