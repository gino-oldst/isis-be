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
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.Keyword;
import st.old.isis.storage.repositories.KeywordRepository;
import st.old.isis.storage.specifications.KeywordSpecification;
import st.old.isis.storage.validators.KeywordValidator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository repository;
    private final KeywordValidator validator;

    private final PolicyMapService policyMapService;

    @Audited(event = "CREATE_KEYWORD")
    public KeywordDto create(@NonNull @Audited final KeywordDto dto) throws IsisException {

        validator.validate(dto);

        var latestNumber = repository.findFirstByOrderByNumberDesc().getNumber();

        if(latestNumber == null){
            latestNumber = Long.valueOf(1);
        } else {
            latestNumber += 1;
        }

        final var entity = mapToEntity(dto);
        entity.setNumber(latestNumber);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    @Audited(event = "UPDATE_KEYWORD")
    public KeywordDto update(@NonNull @Audited final KeywordDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        validator.updateValidate(oldEntity, dto);

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    @Audited(event = "GET_KEYWORD_BY_ID")
    public KeywordDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_KEYWORD")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_KEYWORD")
    public ListDto<KeywordDto> search(@NonNull @Audited final KeywordDto filter) {

        final var spec = new KeywordSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<KeywordDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_KEYWORD")
    public ListDto<KeywordDto> search(@NonNull @Audited final KeywordDto filter, @Audited Pageable pageable) {

        final var spec = new KeywordSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<KeywordDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    @Audited(event = "UPLOAD_KEYWORD_LIST_VIA_API")
    public void uploadKeywordListViaApi(MultipartFile multipartFile, String policyId) throws IOException, IsisException {
        var temp = File.createTempFile(multipartFile.getOriginalFilename(), ".csv");
        temp.deleteOnExit();
        multipartFile.transferTo(temp);
        uploadKeywordList(temp, policyId);
        temp.delete();
    }

    @Audited(event = "UPLOAD_KEYWORD_LIST")
    private void uploadKeywordList(File tempFile, String policyId) throws IsisException {

        log.info("Uploading Keyword List via csv file");

        final var policy = policyMapService.getByPolicyId(policyId);

        try {
            CSVParser csvParser = new CSVParser(new FileReader(tempFile),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            long recordCount = StreamSupport.stream(csvRecords.spliterator(), false).count();
            log.info("Uploading {} keyword records", recordCount);
            int ctr = 1;

            List<KeywordDto> keywordDtoList = new ArrayList<>();    //TODO Can include this in the response for verification

            for(CSVRecord csvRecord: csvRecords){

                final var keywordDto = KeywordDto.builder()
                        .word(csvRecord.get(0).replaceAll("\"", ""))
                        .build();

                final var createdKeyword= create(keywordDto);

                final var policyMapDto = PolicyMapDto.builder()
                        .policyId(policyId)
                        .keywordId(createdKeyword.getId())
                        .build();

                final var createdPolicyMap = policyMapService.create(policyMapDto);

                log.info("Policy Map created for Policy Id: {} and Keyword Id: {}", policyId, createdKeyword.getId());

                keywordDtoList.add(createdKeyword);

            }

        } catch (IOException e) {
            throw new IsisException("Failed to parse CSV file: " + e.getMessage(), "UploadException");
        }


    }

    private Keyword findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find keyword with id: [%s]", id),
                        "NotFoundException"));
    }

    private Keyword mapToEntity(@NonNull final KeywordDto dto) {
        return Keyword.builder()
                .id(dto.getId())
                .number(dto.getNumber())
                .word(dto.getWord())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private KeywordDto mapToResource(@NonNull final Keyword entity) {
        return KeywordDto.builder()
                .id(entity.getId())
                .number(entity.getNumber())
                .word(entity.getWord())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private Keyword mapToEntity(@NonNull Keyword oldEntity, @NonNull KeywordDto newDto) {

        final var entity = mapToEntity(newDto);
        entity.setDateCreated(oldEntity.getDateCreated());

        return entity;
    }
}
