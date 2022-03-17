package st.old.isis.notification.validators;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import st.old.isis.dto.notification.MessageTemplateDto;
import st.old.isis.exception.IsisException;
import st.old.isis.notification.entities.MessageTemplate;
import st.old.isis.notification.repositories.MessageTemplateRepository;

@Component
@RequiredArgsConstructor
public class MessageTemplateValidator {

  private final MessageTemplateRepository repository;

  public void validate(@NonNull final MessageTemplateDto dto) throws IsisException {

    final var isTextTemplateEmpty = Strings.isEmpty(dto.getTextTemplate());

    if (isTextTemplateEmpty) {
      throw IsisException.badRequest("Text template is empty", "ValidationException");
    }
  }

  public void createValidate(@NonNull final MessageTemplateDto dto) throws IsisException {

    final var optTemplate = repository.findByName(dto.getName());

    if (optTemplate.isPresent()) {
      throw IsisException.conflict("Template name already exists", "ConflictException");
    }

    validate(dto);
  }

  public void updateValidate(
      @NonNull final MessageTemplate oldEntity, @NonNull final MessageTemplateDto dto)
      throws IsisException {

    validate(dto);
  }
}
