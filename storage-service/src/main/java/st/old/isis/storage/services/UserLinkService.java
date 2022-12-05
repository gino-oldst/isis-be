package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                    throw IsisException.exception("Error occurred during creation of user link identifier", e.getMessage());
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

        List<UserLinkIdentifierDto> results= new ArrayList<>();

        if(!ListUtils.isNullOrEmpty(dto.getIdentifiers())){
            for(String identifier: dto.getIdentifiers()){
                try {
                    results.add(userLinkIdentifierService.assignFromImport(createdEntity, identifier));
                } catch (IsisException e) {
                    log.error("Error occurred during creation of user link identifier. {}", identifier);
                    throw IsisException.exception("Error occurred during creation of user link identifier", e.getMessage());
                }
            }
        }

        createdEntity.setUserLinkIdentifierDtos(results);

        return createdEntity;
    }

    @Audited(event = "UPDATE_USER_LINK")
    public UserLinkDto update(@NonNull @Audited final UserLinkDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        validator.updateValidate(oldEntity, dto);

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    @Audited(event = "GET_USER_LINK_BY_ID")
    public UserLinkDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_USER_LINK")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

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
        } catch (Exception e) {
            log.info(e.toString());
            throw IsisException.badRequest(e.getMessage(), "InvalidUserLinkException");
        }
    }

    private List<UserLinkDto> processUpload(File tempFile) throws IsisException {
        try{
            CSVParser csvParser = new CSVParser(new FileReader(tempFile),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            long recordCount = StreamSupport.stream(csvRecords.spliterator(), false).count();
            log.info("Importing {} Profile Records", recordCount);

            final Pattern emailPattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

            int ctr = 1;

            List<UserLinkDto> fillUserLinkDtos = new ArrayList<>();

            for (CSVRecord csvRecord : csvRecords) {
                if (recordCount < 100 && ctr % 10 == 0) {
                    log.info("Importing {} of {} , increments of 10 ", ctr, recordCount);
                }

                if (recordCount > 100 && recordCount < 500 && ctr % 50 == 0) {
                    log.info("Importing {} of {} , increments of 50 ", ctr, recordCount);
                }

                if (recordCount > 500 && recordCount < 1000 && ctr % 100 == 0) {
                    log.info("Importing {} of {}, , increments of 100", ctr, recordCount);
                }

                if (recordCount > 1000 && ctr % 500 == 0) {
                    log.info("Importing {} of {} , increments of 500", ctr, recordCount);
                }

                var roughEmails = Arrays.asList(csvRecord.get(4).replaceAll("\"","").split(",",-1));
                var cleanedEmails = roughEmails.stream()
                        .map(String::trim)
                        .filter(emailPattern.asPredicate())
                        .collect(Collectors.toList());

                if (roughEmails.size() != cleanedEmails.size()) {
                    throw new IllegalArgumentException("Invalid Email Format has been found in the CSV File");
                }

                UserLinkDto dto = UserLinkDto.builder()
                        .firstName(csvRecord.get(0).replaceAll("\"",""))
                        .lastName(csvRecord.get(1).replaceAll("\"", ""))
                        .company(csvRecord.get(2).replaceAll("\"",""))
                        .isInternal(csvRecord.get(3).replaceAll("\"", "").equalsIgnoreCase("yes"))
                        .identifiers(cleanedEmails)
                        .build();

                // Change this to add to list after the list is finalized and has no errors.
                // No creation of user link if the list is not good and error free.
                // For now this is for test of creating a single record in externalUser, UserLink, UserLinkIdentifier
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

    //map read-only properties from old entity to new entity
    private UserLink mapToEntity(@NonNull UserLink oldEntity, @NonNull UserLinkDto newDto) {

        final var entity = mapToEntity(newDto);
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
