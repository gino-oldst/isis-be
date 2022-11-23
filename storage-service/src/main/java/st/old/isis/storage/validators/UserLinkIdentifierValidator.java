package st.old.isis.storage.validators;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.dto.storage.UserLinkIdentifierDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.UserLink;
import st.old.isis.storage.repositories.UserLinkIdentifierRepository;
import st.old.isis.storage.repositories.UserLinkRepository;
import st.old.isis.storage.services.ExternalUserService;
import st.old.isis.storage.services.UserLinkService;

@Component
@RequiredArgsConstructor
public class UserLinkIdentifierValidator {
    private final UserLinkIdentifierRepository repository;
    private final UserLinkRepository userLinkRepository;

//    public void createValidate(@NonNull final UserLinkIdentifierDto dto) throws IsisException {
//
//        if(StringUtils.isEmpty(dto.getUserLinkId())) throw IsisException.badRequest("User Link Id is required", "InvalidUserLinkIdentifierException");
//
//        final var userLink = userLinkRepository.getById(dto.getUserLinkId());
//
//        if(userLink == null || userLink.getId() == null){
//            throw IsisException.notFound("User Link Id not found", "UserLinkNotFoundException");
//        }
//
//        if(StringUtils.isEmpty(dto.getIdentifier())) throw IsisException.badRequest("Identifier value is required", "InvalidUserLinkIdentifierException");
//
//        if(StringUtils.isEmpty(dto.getExternalUserId())) throw IsisException.badRequest("External Id is required", "InvalidUserLinkIdentifierException");
//
//        final var optExistingExternalUser = externalUserService.getById(dto.getExternalUserId());
//
//        if (optExistingExternalUser == null || StringUtils.isEmpty(optExistingExternalUser.getId()))  {
//            throw IsisException.notFound("Idenfitier or External User Id does not exists", "ExternalUserFoundException");
//        }
//
//    }

    public void assignValidate(final UserLinkDto dto) throws IsisException {

        if(StringUtils.isEmpty(dto.getId())) throw IsisException.notFound("User Link Id is required", "InvalidUserLinkIdentifierException");

        final var userLink = userLinkRepository.getById(dto.getId());

        if(userLink == null || userLink.getId() == null){
            throw IsisException.notFound("User Link Id not found", "UserLinkNotFoundException");
        }

    }

    public void updateValidate(@NonNull final UserLink oldEntity,
                               @NonNull final UserLinkDto dto) throws IsisException {

    }

}
