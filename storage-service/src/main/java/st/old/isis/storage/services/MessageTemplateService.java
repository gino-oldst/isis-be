package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.dto.analysis.MessageTemplateDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.MessageTemplate;
import st.old.isis.storage.repositories.MessageTemplateRepository;
import st.old.isis.storage.validators.MessageTemplateValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateService {

  private final MessageTemplateRepository repository;
  private final MessageTemplateValidator validator;

  public MessageTemplateDto create(@NonNull final MessageTemplateDto dto) throws IsisException {

    validator.createValidate(dto);

    final var template = mapToEntity(dto);

    final var createdTemplate = repository.save(template);

    return mapToResource(createdTemplate);
  }

  public MessageTemplateDto update(@NonNull final MessageTemplateDto dto) throws IsisException {

    final var oldEntity = findById(dto.getId());

    validator.updateValidate(oldEntity, dto);

    final var newEntity = mapToEntity(oldEntity, dto);

    final var updatedTemplate = repository.save(newEntity);

    return mapToResource(updatedTemplate);
  }

  public MessageTemplateDto getById(@NonNull final String id) throws IsisException {

    final var template = findById(id);

    return mapToResource(template);
  }

  public MessageTemplateDto getByName(@NonNull final String name) throws IsisException {

    final var template = findByName(name);

    return mapToResource(template);
  }

  public void delete(@NonNull final String id) throws IsisException {

    final var template = findById(id);

    repository.delete(template);
  }

  private MessageTemplate findById(@NonNull final String id) throws IsisException {
    return repository
        .findById(id)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find template with id: [%s]", id), "NotFoundException"));
  }

  private MessageTemplate findByName(@NonNull final String name) throws IsisException {
    return repository
        .findByName(name)
        .orElseThrow(
            () ->
                IsisException.notFound(
                    String.format("Cannot find template with name: [%s]", name),
                    "NotFoundException"));
  }

  private MessageTemplate mapToEntity(@NonNull final MessageTemplateDto dto) {
    return MessageTemplate.builder()
        .id(dto.getId())
        .name(dto.getName())
        .textTemplate(dto.getTextTemplate())
        .emailTemplate(dto.getEmailTemplate())
        .type(dto.getType())
        .dateCreated(dto.getDateCreated())
        .dateModified(dto.getDateModified())
        .deleted(dto.isDeleted())
        .build();
  }

  private MessageTemplateDto mapToResource(@NonNull final MessageTemplate entity) {
    return MessageTemplateDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .textTemplate(entity.getTextTemplate())
        .emailTemplate(entity.getEmailTemplate())
        .type(entity.getType())
        .dateCreated(entity.getDateCreated())
        .dateModified(entity.getDateModified())
        .deleted(entity.isDeleted())
        .build();
  }

  // map read-only properties from old entity to new entity
  private MessageTemplate mapToEntity(
      @NonNull MessageTemplate oldEntity, @NonNull MessageTemplateDto newDto) {

    final var entity = mapToEntity(newDto);
    entity.setDateCreated(oldEntity.getDateCreated());

    return entity;
  }
}
