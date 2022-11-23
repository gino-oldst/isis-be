package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.dto.storage.UserLinkIdentifierDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.UserLink;
import st.old.isis.storage.repositories.UserLinkRepository;
import st.old.isis.storage.specifications.UserLinkSpecification;
import st.old.isis.storage.validators.UserLinkValidator;
import st.old.isis.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLinkService {

    private final UserLinkRepository repository;
    private final UserLinkValidator validator;
    private final UserLinkIdentifierService userLinkIdentifierService;

    private final ExternalUserService externalUserService;

    @Audited(event = "CREATE_USER_LINK")
    public UserLinkDto create(@NonNull @Audited final UserLinkDto dto) throws IsisException {

        validator.createValidate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = mapToResource(repository.save(entity));

        List<UserLinkIdentifierDto> results= new ArrayList<>();

        if(!ListUtils.isNullOrEmpty(dto.getIdentifiers())){
            for(String identifier: dto.getIdentifiers()){
                try {
                    results.add(userLinkIdentifierService.assign(createdEntity, identifier));
                } catch (IsisException e) {
                    log.error("Error occurred during creation of user link identifier. {}", identifier);
                    revertFailedCreate(createdEntity, results);
                    throw IsisException.badRequest(e.getMessage(), e.getException());
                }
            }
        }

        createdEntity.setUserLinkIdentifierDtos(results);

        return createdEntity;
    }

    public void revertFailedCreate(UserLinkDto createdUserLink, List<UserLinkIdentifierDto> createdIdentifiers) throws IsisException {
        log.error("Deleting user link {}", createdUserLink.getId());
        this.delete(createdUserLink.getId());

        log.error("Deleting created identifiers");
        createdIdentifiers.stream().forEach(identifier -> {
            externalUserService.removeAssigned(identifier.getExternalUserId());
            userLinkIdentifierService.delete(identifier.getId());
        });
    }

    @Audited(event = "UPDATE_USER_LINK")
    public UserLinkDto update(@NonNull @Audited final UserLinkDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        validator.updateValidate(oldEntity, dto);

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);
        final var updatedEntityDto = mapWithoutIdentifiers(updatedEntity);

        if(!ListUtils.isNullOrEmpty(dto.getIdentifiers())){

            //Handle identifiers
            final var existingIdentifiers = userLinkIdentifierService.findAll(dto.getId());
            final var newIdentifiers = dto.getIdentifiers()
                    .stream()
                    .filter(ide -> !isPreviousIdentifer(ide, existingIdentifiers))
                    .collect(Collectors.toList());

            for(String identifier: newIdentifiers){
                try {
                    userLinkIdentifierService.assign(updatedEntityDto, identifier);
                } catch (IsisException e) {
                    log.error("Error occurred during creation of user link identifier. {}", identifier);
                    throw IsisException.badRequest(e.getMessage(), e.getException());
                }
            }

            final var deleteIdentifiers = existingIdentifiers
                    .stream()
                    .filter(ide -> !isPreviousIdentifer(ide, dto.getIdentifiers()))
                    .collect(Collectors.toList());

            for(UserLinkIdentifierDto identifier: deleteIdentifiers){
                userLinkIdentifierService.delete(identifier);
            }

        }

        return mapToResource(updatedEntity);
    }

    private boolean isPreviousIdentifer(String ide, List<UserLinkIdentifierDto> existingIdentifiers) {
        return existingIdentifiers.stream().anyMatch(ei -> ei.getIdentifier().equals(ide));
    }

    private boolean isPreviousIdentifer(UserLinkIdentifierDto ide, List<String> existingIdentifiers) {
        return existingIdentifiers.stream().anyMatch(ei -> ei.equals(ide.getIdentifier()));
    }


    @Audited(event = "GET_USER_LINK_BY_ID")
    public UserLinkDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_USER_LINK")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);
        final var existingDto = mapToResource(entity);

        if(!ListUtils.isNullOrEmpty(existingDto.getUserLinkIdentifierDtos()))
        for(UserLinkIdentifierDto identifier: existingDto.getUserLinkIdentifierDtos()){
            userLinkIdentifierService.delete(identifier);
        }

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_USER_LINK")
    public ListDto<UserLinkDto> search(@NonNull @Audited final UserLinkDto filter) {

        final var spec = new UserLinkSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<UserLinkDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_USER_LINK")
    public ListDto<UserLinkDto> search(@NonNull @Audited final UserLinkDto filter, @Audited Pageable pageable) {

        final var spec = new UserLinkSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<UserLinkDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private UserLink findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find user link with id: [%s]", id),
                        "NotFoundException"));
    }

    private UserLink mapToEntity(@NonNull final UserLinkDto dto) {
        return UserLink.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .externalUserId(dto.getExternalUserId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .company(dto.getCompany())
                .externalCompanyId(dto.getExternalCompanyId())
                .isInternal(dto.isInternal())
                .deleted(dto.isDeleted())
                .build();
    }

    private UserLinkDto mapToResource(@NonNull final UserLink entity) {

        final var identifierList= userLinkIdentifierService.findAll(entity.getId());

        return UserLinkDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .externalUserId(entity.getExternalUserId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .company(entity.getCompany())
                .externalCompanyId(entity.getExternalCompanyId())
                .isInternal(entity.isInternal())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .userLinkIdentifierDtos(identifierList)
                .build();
    }

    private UserLinkDto mapWithoutIdentifiers(@NonNull final UserLink entity) {
        return UserLinkDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .externalUserId(entity.getExternalUserId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .company(entity.getCompany())
                .externalCompanyId(entity.getExternalCompanyId())
                .isInternal(entity.isInternal())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private UserLink mapToEntity(@NonNull UserLink oldEntity, @NonNull UserLinkDto newDto) {

        final var entity = mapToEntity(newDto);
        entity.setId(oldEntity.getId());
        entity.setDateCreated(oldEntity.getDateCreated());

        return entity;
    }
}
