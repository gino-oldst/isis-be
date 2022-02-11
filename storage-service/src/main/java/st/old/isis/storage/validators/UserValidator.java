package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.UserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.User;

@Component
@RequiredArgsConstructor
public class UserValidator {

    public void validate(@NonNull final UserDto dto) throws IsisException {

    }

    public void updateValidate(@NonNull final User oldEntity,
                               @NonNull final UserDto dto) throws IsisException {

    }
}
