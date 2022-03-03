package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.CommunicationChainDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationChain;

@Component
@RequiredArgsConstructor
public class CommunicationChainValidator {

    public void validate(@NonNull final CommunicationChainDto dto) throws IsisException {
        //todo: add validations for communication chain
    }

    public void updateValidate(@NonNull final CommunicationChain oldEntity,
                               @NonNull final CommunicationChainDto dto) throws IsisException {

    }
}
