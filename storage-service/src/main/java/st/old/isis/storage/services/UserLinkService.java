package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.dto.storage.UserLinkIdentifierDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.User;
import st.old.isis.storage.entities.UserLink;
import st.old.isis.storage.repositories.UserLinkRepository;
import st.old.isis.storage.specifications.UserLinkSpecification;
import st.old.isis.storage.validators.UserLinkValidator;
import st.old.isis.utils.ListUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    @Audited(event = "CREATE_USER_LINK_FROM_IMPORT")
    public UserLinkDto createUserLinkFromImport(@NonNull @Audited final UserLinkDto dto) throws IsisException {

        validator.createValidate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = mapToResource(repository.save(entity));

        List<UserLinkIdentifierDto> results = new ArrayList<>();

        if (!ListUtils.isNullOrEmpty(dto.getIdentifiers())) {
            for (String identifier : dto.getIdentifiers()) {
                try {
                    results.add(userLinkIdentifierService.assignFromImport(createdEntity, identifier));
                } catch (IsisException e) {
                    log.error("Error occurred during creation of user link identifier. {}", identifier);
                    revertFailedCreate(createdEntity, results);
                    throw IsisException.exception("Error occurred during creation of user link identifier", e.getMessage());
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

    @Audited(event = "UPLOAD_USER_LINK")
    public ListDto<UserLinkDto> upload(MultipartFile multipartFile) throws IOException, IsisException {
        var temp = File.createTempFile(multipartFile.getOriginalFilename(), ".csv");
        temp.deleteOnExit();
        multipartFile.transferTo(temp);
        try{
            var results = processUpload(temp);

            // Add Verified UserLinks
            for (UserLinkDto userLinkDto : results){
                createUserLinkFromImport(userLinkDto);
            }

            // Delete the temporary CSV File
            temp.delete();

            log.info("Completed Profile Records Data Upload");
            return ListDto.<UserLinkDto>builder()
                    .data(results)
                    .totalNoOfRecords(results.size())
                    .build()
                    .success();
        } catch (IsisException e) {
            log.info(e.toString());
            throw IsisException.badRequest(e.getMessage(), e.getException());
        }
    }

    private boolean isOnPreceedingCSVRows(List<UserLinkDto> csvList, String str) {
        List<UserLinkDto> foundUser = csvList.stream()
                    .filter(userLink -> userLink.getIdentifiers().stream()
                            .anyMatch(identifier ->
                                    identifier.equals(str)))
                            .collect(Collectors.toList());
        return foundUser.size() > 0;
    }

    public static boolean isAlphaNumeric(String s) {
        return s != null && !s.matches("^[a-zA-Z]*$");
    }

    private List<UserLinkDto> processUpload(File tempFile) throws IsisException {
        try{
            CSVParser csvParser = new CSVParser(new FileReader(tempFile),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            Iterable<CSVRecord> csvRecords = csvParser.getRecords().stream()
                    .filter(item -> !Strings.isEmpty(item.get(0)))
                    .collect(Collectors.toList());

            long recordCount = StreamSupport.stream(csvRecords.spliterator(), false).count();
            log.info("Importing {} Profile Records", recordCount);

            final Pattern emailPattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

            int ctr = 1;

            List<UserLinkDto> fillUserLinkDtos = new ArrayList<>();

            for (CSVRecord csvRecord : csvRecords) {
                var identifiers = Arrays
                        .asList(csvRecord.get(4).replaceAll("\"","").split(",",-1)).stream()
                        .filter(item -> !Strings.isEmpty(item))
                        .map(String::trim)
                        .collect(Collectors.toList());

                if (isAlphaNumeric(csvRecord.get(0).replaceAll("\"",""))) {
                    final var error = String.format("On Row %s. First Name should not contain numbers and special characters", ctr);
                    log.error(error);
                    throw IsisException.conflict(error, "InvalidField");
                }

                if (isAlphaNumeric(csvRecord.get(1).replaceAll("\"",""))) {
                    final var error = String.format("On Row %s. Last Name should not contain numbers and special characters", ctr);
                    log.error(error);
                    throw IsisException.conflict(error, "InvalidField");
                }

                if (csvRecord.get(0).replaceAll("\"","").isEmpty()) {
                    final var error = String.format("On Row %s. First Name should not be Empty", ctr);
                    log.error(error);
                    throw IsisException.conflict(error, "InvalidField");
                }

                if (csvRecord.get(1).replaceAll("\"","").isEmpty()) {
                    final var error = String.format("On Row %s. Last Name should not be Empty", ctr);
                    log.error(error);
                    throw IsisException.conflict(error, "InvalidField");
                }

                if (csvRecord.get(2).replaceAll("\"","").isEmpty()) {
                    final var error = String.format("On Row %s. Company Name should not be Empty", ctr);
                    log.error(error);
                    throw IsisException.conflict(error, "InvalidField");
                }

                for (String str : identifiers){
                    Matcher m = emailPattern.matcher(str.trim());
                    ExternalUserDto externalUser = null;

                    if (!m.matches() && !str.isEmpty()) {
                        log.error(String.format("Error on row %s", ctr));
                        throw new IllegalArgumentException("Invalid Email Format has been found in the CSV File");
                    }

                    if (fillUserLinkDtos.size() > 0) {
                        if (isOnPreceedingCSVRows(fillUserLinkDtos, str.trim())){
                            final var error = String.format("On Row %s. Identifier %s is already assigned", ctr, str.trim());
                            log.error(error);
                            throw IsisException.conflict(error, "AlreadyAssignedException");
                        }
                    }

                    try{
                        externalUser = externalUserService.getByEmail(str.trim());

                        if(externalUser.getAssignedTo() != null){
                            final var error = String.format("On Row %s. Identifier %s is already assigned", ctr, str.trim());
                            log.error(error);
                            throw IsisException.conflict(error, "AlreadyAssignedException");
                        }

                    } catch (IsisException e) {
                        if (e.getException() == "AlreadyAssignedException") {
                            throw e;
                        }
                        log.error(e.getMessage());
                    }
                }

                UserLinkDto dto = UserLinkDto.builder()
                        .firstName(csvRecord.get(0).replaceAll("\"",""))
                        .lastName(csvRecord.get(1).replaceAll("\"", ""))
                        .company(csvRecord.get(2).replaceAll("\"",""))
                        .isInternal(csvRecord.get(3).replaceAll("\"", "").equalsIgnoreCase("yes"))
                        .identifiers(identifiers.size() == 0 ? null : identifiers )
                        .build();

                // Change this to add to list after the list is finalized and has no errors.
                // No creation of user link if the list is not good and error free.
                // For now this is for test of creating a single record in externalUser, UserLink, UserLinkIdentifier
                //  createUserLinkFromImport(dto);
                fillUserLinkDtos.add(dto);
                ctr++;
            }

            log.info("Completed Profile Records Data Verification");
            return fillUserLinkDtos;
        } catch(IOException e) {
            log.info("File Upload Failed");
            throw new IllegalArgumentException("Error occurred while uploading the CSV File");
        }
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

    private List<UserLink> mapToEntityList(List<UserLinkDto> userLinkDtos) {

        return userLinkDtos.stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());
    }

    private List<UserLinkDto> mapToDtoList(List<UserLink> fills) {

        return fills.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());
    }
}
