package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.UserDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.User;
import st.old.isis.storage.repositories.UserRepository;
import st.old.isis.storage.specifications.UserSpecification;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public UserDto create(@NonNull final UserDto dto) {

        final var entity = mapToEntity(dto);

        final var createdEntity = repository.save(entity);

        return mapToResource(createdEntity);
    }

    public UserDto update(@NonNull final UserDto dto) {

        final var newEntity = mapToEntity(dto);

        final var updatedEntity = repository.save(newEntity);

        return mapToResource(updatedEntity);
    }

    public UserDto getById(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        return mapToResource(entity);
    }

    public void delete(@NonNull final String id) throws IsisException {

        final var entity = findById(id);

        repository.delete(entity);
    }

    public ListDto<UserDto> search(@NonNull final UserDto filter) {

        final var spec = new UserSpecification(filter);

        final var results = repository.findAll(spec)
                .stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        final var size = results.size();

        return ListDto.<UserDto>builder()
                .data(results)
                .totalNoOfRecords(size)
                .totalPages(size)
                .totalCurrentElements(size)
                .build()
                .success();
    }

    public ListDto<UserDto> search(@NonNull final UserDto filter, Pageable pageable) {

        final var spec = new UserSpecification(filter);

        final var searchResults = repository.findAll(spec, pageable);

        final var results = searchResults.stream()
                .map(this::mapToResource)
                .collect(Collectors.toList());

        return ListDto.<UserDto>builder()
                .data(results)
                .totalNoOfRecords(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .totalCurrentElements(searchResults.getNumberOfElements())
                .build()
                .success();
    }

    private User findById(@NonNull final String id) throws IsisException {
        return repository.findById(id)
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find user with id: [%s]", id),
                        "NotFoundException"));
    }

    private User mapToEntity(@NonNull final UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateCreated(dto.getDateCreated())
                .dateModified(dto.getDateModified())
                .deleted(dto.isDeleted())
                .build();
    }

    private UserDto mapToResource(@NonNull final User entity) {
        return UserDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateCreated(entity.getDateCreated())
                .dateModified(entity.getDateModified())
                .deleted(entity.isDeleted())
                .build();
    }
}
