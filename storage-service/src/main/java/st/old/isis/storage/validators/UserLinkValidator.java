package st.old.isis.storage.validators;

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

        validate(dto);

        final var userId = dto.getUserId();
        final var externalUserId = dto.getExternalUserId();

        final var optUserLink = repository.findByUserIdAndExternalUserId(userId, externalUserId);

        if (optUserLink.isPresent()) {
            throw IsisException.conflict("User link already exists", "ConflictException");
        }
    }

    public void updateValidate(@NonNull final UserLink oldEntity,
                               @NonNull final UserLinkDto dto) throws IsisException {

    }

    private void validate(@NonNull final UserLinkDto dto) throws IsisException {

        //check if user id and external id are existing
        userService.getById(dto.getUserId());
        externalUserService.getById(dto.getExternalUserId());
    }
}
