package st.old.isis.storage.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AlertPolicy;
import st.old.isis.storage.repositories.AlertPolicyRepository;
import st.old.isis.storage.specifications.AlertPolicySpecification;
import st.old.isis.storage.validators.AlertPolicyValidator;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertPolicyService {

    private final AlertPolicyRepository repository;
    private final AlertPolicyValidator validator;

    public AlertPolicyDto create(@NonNull final AlertPolicyDto dto) throws IsisException{

        validator.validate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public AlertPolicyDto update(@NonNull final AlertPolicyDto dto) throws IsisException{

        final var oldEntity = findById(dto.getId());

        validator.validate(dto);

        final var newEntity = mapToEntity(oldEntity, dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    public AlertPolicyDto getById(@NonNull final String id) throws IsisException{

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public AlertPolicyDto getByName(@NonNull final String name) throws IsisException {

        final var template = findByName(name);

        return mapToResource(template);
    }

    public AlertPolicyDto getByInternalCode(@NonNull final String internalCode) throws IsisException {

        final var template = findByInternalCode(internalCode);

        return mapToResource(template);
    }

    public void delete(@NonNull final String id) throws IsisException{

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<AlertPolicyDto> search(@NonNull final AlertPolicyDto filter){

        final var spec =  new AlertPolicySpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<AlertPolicyDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    public ListDto<AlertPolicyDto> search(@NonNull final AlertPolicyDto filter, Pageable pageable){

        final var spec = new AlertPolicySpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<AlertPolicyDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private AlertPolicy findById(@NonNull final String id) throws IsisException{
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                       String.format("Cannot find Policy with id: [%s]", id),
                       "NotFoundException"));
    }

    private AlertPolicy findByName(@NonNull final String name) throws IsisException {
        return repository.findByName(name)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find alert with name: [%s]", name),
                        "NotFoundException"));
    }

    private AlertPolicy findByInternalCode(@NonNull final String internalCode) throws IsisException {
        return repository.findByInternalCode(internalCode)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find alert with internal code: [%s]", internalCode),
                        "NotFoundException"));
    }

    private AlertPolicy mapToEntity(@NonNull final AlertPolicyDto dto){
        return AlertPolicy.builder()
                .id(dto.getId())
                .name(dto.getName())
                .internalCode(dto.getInternalCode())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private AlertPolicyDto mapToResource(@NonNull final AlertPolicy entity){
        return AlertPolicyDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .internalCode(entity.getInternalCode())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }

    //map read-only properties from old entity to new entity
    private AlertPolicy mapToEntity(@NonNull AlertPolicy oldEntity, @NonNull AlertPolicyDto newDto) {

        final var entity = mapToEntity(newDto);
        entity.setDateCreated(oldEntity.getDateCreated());

        return entity;
    }
}
