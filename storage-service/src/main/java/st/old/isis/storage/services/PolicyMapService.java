package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.PolicyMap;
import st.old.isis.storage.repositories.PolicyMapRepository;
import st.old.isis.storage.specifications.PolicyMapSpecification;
import st.old.isis.storage.validators.PolicyMapValidator;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyMapService {

    private final PolicyMapRepository repository;
    private final PolicyMapValidator validator;

    @Audited(event = "CREATE_POLICY_MAP")
    public PolicyMapDto create(@NonNull @Audited final PolicyMapDto dto) throws IsisException {

        validator.validate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    @Audited(event = "UPDATE_POLICY_MAP")
    public PolicyMapDto update(@NonNull @Audited final PolicyMapDto dto) throws IsisException {

        final var oldEntity = findById(dto.getId());

        validator.updateValidate(oldEntity, dto);

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    @Audited(event = "GET_POLICY_MAP_BY_ID")
    public PolicyMapDto getById(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    @Audited(event = "DELETE_POLICY_MAP")
    public void delete(@NonNull @Audited final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    @Audited(event = "SEARCH_POLICY_MAP")
    public ListDto<PolicyMapDto> search(@NonNull @Audited final PolicyMapDto filter) {

        final var spec = new PolicyMapSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<PolicyMapDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    @Audited(event = "SEARCH_POLICY_MAP")
    public ListDto<PolicyMapDto> search(@NonNull @Audited final PolicyMapDto filter, @Audited Pageable pageable) {

        final var spec = new PolicyMapSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<PolicyMapDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private PolicyMap findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find policy map with id: [%s]", id),
                        "NotFoundException"));
    }

    private PolicyMap mapToEntity(@NonNull final PolicyMapDto dto) {
        return PolicyMap.builder()
                .id(dto.getId())
                .keywordId(dto.getKeywordId())
                .policyId(dto.getPolicyId())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private PolicyMapDto mapToResource(@NonNull final PolicyMap entity) {
        return PolicyMapDto.builder()
                .id(entity.getId())
                .keywordId(entity.getKeywordId())
                .policyId(entity.getPolicyId())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private PolicyMap mapToEntity(@NonNull PolicyMap oldEntity, @NonNull PolicyMapDto newDto) {

        final var entity = mapToEntity(newDto);
        entity.setDateCreated(oldEntity.getDateCreated());

        return entity;
    }
}
