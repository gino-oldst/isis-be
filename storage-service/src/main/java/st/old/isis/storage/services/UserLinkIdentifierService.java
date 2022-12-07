package st.old.isis.storage.services;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.dto.storage.UserLinkIdentifierDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.UserLinkIdentifier;
import st.old.isis.storage.repositories.UserLinkIdentifierRepository;
import st.old.isis.storage.validators.UserLinkIdentifierValidator;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLinkIdentifierService {

    private final UserLinkIdentifierRepository repository;

    private final UserLinkIdentifierValidator validator;

    private final ExternalUserService externalUserService;

    @Audited(event = "CREATE_USER_LINK_IDENTIFIER")
    public UserLinkIdentifierDto create(@NonNull @Audited final UserLinkIdentifierDto dto) throws IsisException {

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public UserLinkIdentifierDto assign(@NonNull @Audited final UserLinkDto userLinkDto, @NonNull String identifier) throws IsisException {

        validator.assignValidate(userLinkDto);

        if(StringUtils.isEmpty(identifier)) throw IsisException.badRequest("Identifier value is required", "InvalidUserLinkIdentifierException");

        final var externalUser = externalUserService.getByEmail(identifier);

        if (externalUser == null || StringUtils.isEmpty(externalUser.getId()))  {
            throw IsisException.notFound("Identifier or External User does not exist", "ExternalUserFoundException");
        }

        if(externalUser.getAssignedTo() != null){
            final var error = String.format("Identifier %s is already assigned to user %s", identifier, externalUser.getAssignedTo());
            log.error(error);
            throw IsisException.conflict(error, "AlreadyAssignedException");
        }

        final var dto = UserLinkIdentifierDto.builder()
                .userLinkId(userLinkDto.getId())
                .externalUserId(externalUser.getId())
                .identifier(externalUser.getEmail())
                .build();

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        externalUser.setAssignedTo(userLinkDto.getId());
        externalUserService.update(externalUser);

        return mapToResource(createdEntity);
    }

    public UserLinkIdentifierDto assignFromImport(@NonNull @Audited final UserLinkDto userLinkDto, @NonNull String identifier) throws IsisException {

        validator.assignValidate(userLinkDto);

        if (StringUtils.isEmpty(identifier))
            throw IsisException.badRequest("Identifier value is required", "InvalidUserLinkIdentifierException");

        ExternalUserDto externalUser = null;

        try {
            externalUser = externalUserService.getByEmail(identifier);
            if(externalUser.getAssignedTo() != null){
                final var error = String.format("Identifier %s is already assigned to user %s", identifier, externalUser.getAssignedTo());
                log.error(error);
                throw IsisException.conflict(error, "AlreadyAssignedException");
            }
        } catch (IsisException e) {
            log.info(e.getMessage());
        }

        if (externalUser == null || StringUtils.isEmpty(externalUser.getId())) {
            // CREATE EXTERNAL
            log.info(String.format("Creating new external user with id: [%s]", identifier));

            final var externalUserDto = ExternalUserDto.builder()
                    .firstName(userLinkDto.getFirstName())
                    .lastName(userLinkDto.getLastName())
                    .email(identifier)
                    .externalId(identifier)
                    .build();

            externalUser = externalUserService.create(externalUserDto);

            log.info(String.format("Created new external user with id: [%s]", identifier));
        }

        final var dto = UserLinkIdentifierDto.builder()
                .userLinkId(userLinkDto.getId())
                .externalUserId(externalUser.getId())
                .identifier(externalUser.getEmail())
                .build();

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        externalUser.setAssignedTo(userLinkDto.getId());
        externalUserService.update(externalUser);

        return mapToResource(createdEntity);
    }

    public void delete(@NonNull final UserLinkIdentifierDto identifierDto) throws IsisException {

        final var externalUser = externalUserService.getById(identifierDto.getExternalUserId());

        if (externalUser == null || StringUtils.isEmpty(externalUser.getId()))  {
            throw IsisException.notFound("Identifier or External User does not exist", "ExternalUserNotFoundException");
        }

        final var existingUserLinkIdentifier= repository.findById(identifierDto.getId());

        if (existingUserLinkIdentifier.isEmpty())  {
            throw IsisException.notFound("User Link does not exist", "UserLinkNotFoundException");
        }

        repository.delete(existingUserLinkIdentifier.get());

        externalUser.setAssignedTo(null);
        externalUserService.update(externalUser);

    }

    @Audited(event = "DELETE_USER_LINK_IDENTIFIER")
    public void delete(@NonNull @Audited final String id) {
        repository.deleteById(id);
    }

    public List<UserLinkIdentifierDto> findAll(@NonNull final String userLinkId){

        final var results = repository.findAllByUserLinkId(userLinkId);

        return results.stream().map(this::mapToResource).collect(Collectors.toList());

    }

    public UserLinkIdentifierDto mapToResource(@NonNull final UserLinkIdentifier entity) {
        return UserLinkIdentifierDto.builder()
                .id(entity.getId())
                .userLinkId(entity.getUserLinkId())
                .identifier(entity.getIdentifier())
                .source(entity.getSource())
                .externalUserId(entity.getIdentifierId())
                .identifierType(entity.getIdentifierType())
                .importDate(entity.getImportDate())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }
    public UserLinkIdentifier mapToEntity(@NonNull final UserLinkIdentifierDto dto) {
        return UserLinkIdentifier.builder()
                .id(dto.getId())
                .userLinkId(dto.getUserLinkId())
                .identifier(dto.getIdentifier())
                .source(dto.getSource())
                .identifierId(dto.getExternalUserId())
                .identifierType(dto.getIdentifierType())
                .importDate(dto.getImportDate())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

}
