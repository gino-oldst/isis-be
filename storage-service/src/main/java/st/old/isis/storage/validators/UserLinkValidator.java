package st.old.isis.storage.validators;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.UserLink;
import st.old.isis.storage.repositories.UserLinkRepository;
import st.old.isis.storage.services.ExternalUserService;
import st.old.isis.storage.services.UserService;

@Component
@RequiredArgsConstructor
public class UserLinkValidator {

    private final UserService userService;
    private final ExternalUserService externalUserService;
    private final UserLinkRepository repository;

    public void createValidate(@NonNull final UserLinkDto dto) throws IsisException {

        commonValidate(dto);

        final var optExistingUserLink = repository.findByFirstNameAndLastNameAndCompany(dto.getFirstName(), dto.getLastName(), dto.getCompany());

        if (optExistingUserLink.isPresent()) {
            throw IsisException.conflict("User link already exists", "ConflictException");
        }

    }

    public void updateValidate(@NonNull final UserLink oldEntity,
                               @NonNull final UserLinkDto dto) throws IsisException {
        commonValidate(dto);

        if(oldEntity.getId() == null){
            throw IsisException.badRequest("No user link id indicated", "MissingIdException");
        }

        handleNameOrCompanyChanges(oldEntity, dto);

    }

    private void handleNameOrCompanyChanges(@NonNull final UserLink oldEntity,
                                            @NonNull final UserLinkDto dto) throws IsisException {

        if(!oldEntity.getFirstName().equals(dto.getFirstName())
            || !oldEntity.getLastName().equals(dto.getLastName())
            || !oldEntity.getCompany().equals(dto.getCompany())){
            final var optExistingUserLink = repository.findByFirstNameAndLastNameAndCompany(dto.getFirstName(), dto.getLastName(), dto.getCompany());

            if (optExistingUserLink.isPresent()) {
                throw IsisException.conflict("User link already exists", "ConflictException");
            }
        }

    }

    public void commonValidate(@NonNull final UserLinkDto dto) throws IsisException {

        if(StringUtils.isEmpty(dto.getFirstName())) throw IsisException.badRequest("First Name is required", "InvalidUserLinkException");

        if(StringUtils.isEmpty(dto.getLastName())) throw IsisException.badRequest("Last Name is required", "InvalidUserLinkException");

        if(StringUtils.isEmpty(dto.getCompany())) throw IsisException.badRequest("Company is required", "InvalidUserLinkException");

    }

    private void validate(@NonNull final UserLinkDto dto) throws IsisException {

        //check if user id and external id are existing
        userService.getById(dto.getUserId());
        externalUserService.getById(dto.getExternalUserId());
    }
}
