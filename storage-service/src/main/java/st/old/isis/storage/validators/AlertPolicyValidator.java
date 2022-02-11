package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.Keyword;
import st.old.isis.storage.repositories.AlertPolicyRepository;

@Component
@RequiredArgsConstructor
public class AlertPolicyValidator {
    private final AlertPolicyRepository repository;

    public void validate(@NonNull final AlertPolicyDto dto) throws IsisException {

        final var optName = repository.findByName(dto.getName());

        if (optName.isPresent()) {
            throw IsisException.conflict("Name already exists", "ConflictException");
        }
    }

    public void updateValidate(@NonNull final Keyword oldEntity,
                               @NonNull final KeywordDto dto) throws IsisException {

    }
}
