package st.old.isis.notification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.notification.MessageTemplateDto;
import st.old.isis.exception.IsisException;
import st.old.isis.notification.entities.MessageTemplate;
import st.old.isis.notification.repositories.MessageTemplateRepository;
import st.old.isis.notification.validators.MessageTemplateValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateService {

  private final MessageTemplateRepository repository;
  private final MessageTemplateValidator validator;

  @Audited(event = "CREATE_MESSAGE_TEMPLATE")
  public MessageTemplateDto create(@NonNull @Audited final MessageTemplateDto dto)
      throws IsisException {

    validator.createValidate(dto);

    final var template = mapToEntity(dto);

    final var createdTemplate = repository.save(template);

    return mapToResource(createdTemplate);
  }

  @Audited(event = "UPDATE_MESSAGE_TEMPLATE")
  public MessageTemplateDto update(@NonNull @Audited final MessageTemplateDto dto)
      throws IsisException {

    final var oldEntity = findById(dto.getId());

    validator.updateValidate(oldEntity, dto);

    final var newEntity = mapToEntity(oldEntity, dto);

    final var updatedTemplate = repository.save(newEntity);

    return mapToResource(updatedTemplate);
  }

  @Audited(event = "GET_MESSAGE_TEMPLATE_BY_ID")
  public MessageTemplateDto getById(@NonNull @Audited final String id) throws IsisException {

    final var template = findById(id);

    return mapToResource(template);
  }

  @Audited(event = "GET_MESSAGE_TEMPLATE_BY_NAME")
  public MessageTemplateDto getByName(@NonNull @Audited final String name) throws IsisException {

    final var template = findByName(name);

    return mapToResource(template);
  }

  @Audited(event = "DELETE_MESSAGE_TEMPLATE")
  public void delete(@NonNull @Audited final String id) throws IsisException {

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
        .subject(dto.getSubject())
        .textTemplate(dto.getTextTemplate())
        .emailTemplate(dto.getEmailTemplate())
        .dateCreated(dto.getDateCreated())
        .dateModified(dto.getDateModified())
        .deleted(dto.isDeleted())
        .build();
  }

  private MessageTemplateDto mapToResource(@NonNull final MessageTemplate entity) {
    return MessageTemplateDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .subject(entity.getSubject())
        .textTemplate(entity.getTextTemplate())
        .emailTemplate(entity.getEmailTemplate())
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
