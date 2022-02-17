package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.AppUserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AppUser;
import st.old.isis.utils.ListUtils;

@Component
@RequiredArgsConstructor
public class AppUserValidator {

  public void validate(@NonNull final AppUserDto dto) throws IsisException {}

  public void createValidate(@NonNull final AppUserDto dto) throws IsisException {

    final var username = dto.getUsername();

    if (Strings.isEmpty(username)) {
      throw IsisException.badRequest("Username is empty", "ValidationException");
    }

    final var password = dto.getPassword();
    final var password2 = dto.getPassword2();

    final var passwordInvalid = Strings.isEmpty(password) || Strings.isEmpty(password2);

    if (passwordInvalid) {
      throw IsisException.badRequest("Password is invalid", "ValidationException");
    }

    final var notMatch = !password.equals(password2);

    if (notMatch) {
      throw IsisException.badRequest("Passwords do not match", "ValidationException");
    }

    if (ListUtils.isNullOrEmpty(dto.getRoles())) {
      throw IsisException.badRequest("Roles are missing", "ValidationException");
    }
  }

  public void updateValidate(@NonNull final AppUser oldEntity, @NonNull final AppUserDto dto)
      throws IsisException {}
}
