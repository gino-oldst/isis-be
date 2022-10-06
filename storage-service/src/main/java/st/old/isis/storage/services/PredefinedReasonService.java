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
import st.old.isis.dto.storage.PredefinedReasonDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.Keyword;
import st.old.isis.storage.entities.PredefinedReason;
import st.old.isis.storage.repositories.KeywordRepository;
import st.old.isis.storage.repositories.PredefinedReasonRepository;
import st.old.isis.storage.specifications.KeywordSpecification;
import st.old.isis.storage.specifications.PredefinedReasonSpecification;
import st.old.isis.storage.validators.KeywordValidator;
import st.old.isis.storage.validators.PredefinedReasonValidator;

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
public class PredefinedReasonService {

    private final PredefinedReasonRepository repository;
    private final PredefinedReasonValidator validator;

    @Audited(event = "CREATE_PREDEFINED_REASON")
    public PredefinedReasonDto create(@NonNull @Audited final PredefinedReasonDto dto) throws IsisException {

        validator.validate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    @Audited(event = "UPDATE_PREDEFINED_REASON")
    public PredefinedReasonDto update(@NonNull @Audited final PredefinedReasonDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    @Audited(event = "GET_PREDEFINED_REASON_BY_ID")
    public PredefinedReasonDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_PREDEFINED_REASON")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_KEYWORD")
    public ListDto<PredefinedReasonDto> search(@NonNull @Audited final PredefinedReasonDto filter) {

        final var spec = new PredefinedReasonSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<PredefinedReasonDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_PREDEFINED_REASON")
    public ListDto<PredefinedReasonDto> search(@NonNull @Audited final PredefinedReasonDto filter, @Audited Pageable pageable) {

        final var spec = new PredefinedReasonSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<PredefinedReasonDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private PredefinedReason findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find predefined reason with id: [%s]", id),
                        "NotFoundException"));
    }

    private PredefinedReason mapToEntity(@NonNull final PredefinedReasonDto dto) {
        return PredefinedReason.builder()
                .id(dto.getId())
                .value(dto.getValue())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .addedByAppUser(dto.getAddedByAppUser())
                .deleted(dto.isDeleted())
                .build();
    }

    private PredefinedReasonDto mapToResource(@NonNull final PredefinedReason entity) {
        return PredefinedReasonDto.builder()
                .id(entity.getId())
                .value(entity.getValue())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .addedByAppUser(entity.getAddedByAppUser())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private PredefinedReason mapToEntity(@NonNull PredefinedReason oldEntity, @NonNull PredefinedReasonDto newDto) {

        final var entity = mapToEntity(newDto);
        entity.setDateCreated(oldEntity.getDateCreated());

        return entity;
    }
}
