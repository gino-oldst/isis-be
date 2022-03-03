package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.Keyword;
import st.old.isis.storage.repositories.KeywordRepository;
import st.old.isis.storage.specifications.KeywordSpecification;
import st.old.isis.storage.validators.KeywordValidator;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository repository;
    private final KeywordValidator validator;

    @Audited(event = "CREATE_KEYWORD")
    public KeywordDto create(@NonNull @Audited final KeywordDto dto) throws IsisException {

        validator.validate(dto);
        
        final var entity = mapToEntity(dto);

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

    private Keyword findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find keyword with id: [%s]", id),
                        "NotFoundException"));
    }

    private Keyword mapToEntity(@NonNull final KeywordDto dto) {
        return Keyword.builder()
                .id(dto.getId())
                .word(dto.getWord())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private KeywordDto mapToResource(@NonNull final Keyword entity) {
        return KeywordDto.builder()
                .id(entity.getId())
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
