package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.PredefinedReasonDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.repositories.AppUserRepository;
import st.old.isis.storage.repositories.PredefinedReasonRepository;
import st.old.isis.storage.services.AppUserService;

@Component
@RequiredArgsConstructor
public class PredefinedReasonValidator {

    private final AppUserService appUserService;

    public void validate(@NonNull final PredefinedReasonDto dto) throws IsisException {

        if (StringUtils.isEmpty(dto.getValue())) {
            throw IsisException.badRequest("Predefined reason value is empty ", "EmptyValueException");
        }

        if (StringUtils.isEmpty(dto.getAddedByAppUser())) {
            throw IsisException.notFound("Predefined reason must be added by a valid app user", "NotFoundException");
        } else {
            final var appUser = appUserService.getById(dto.getAddedByAppUser());

            if(appUser == null){
                throw IsisException.notFound("Predefined reason must be added by a valid app user", "NotFoundException");
            }
        }
    }

}
