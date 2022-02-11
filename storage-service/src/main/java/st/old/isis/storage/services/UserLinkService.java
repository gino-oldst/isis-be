package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.UserLinkDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.UserLink;
import st.old.isis.storage.repositories.UserLinkRepository;
import st.old.isis.storage.specifications.UserLinkSpecification;
import st.old.isis.storage.validators.UserLinkValidator;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLinkService {

    private final UserLinkRepository repository;
    private final UserLinkValidator validator;

    public UserLinkDto create(@NonNull final UserLinkDto dto) throws IsisException {

        validator.createValidate(dto);

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public UserLinkDto update(@NonNull final UserLinkDto dto) throws IsisException {

        validator.validate(dto);

        final var newEntity = mapToEntity(dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    public UserLinkDto getById(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public void delete(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<UserLinkDto> search(@NonNull final UserLinkDto filter) {

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

    public ListDto<UserLinkDto> search(@NonNull final UserLinkDto filter, Pageable pageable) {

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
                .dateCreated(dto.getDateCreated())
                .deleted(dto.isDeleted())
                .build();
    }

    private UserLinkDto mapToResource(@NonNull final UserLink entity) {
        return UserLinkDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .externalUserId(entity.getExternalUserId())
                .dateCreated(entity.getDateCreated())
                .deleted(entity.isDeleted())
                .build();
    }
}
