package st.old.isis.analysis.validators;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import st.old.isis.analysis.repositories.MessageTemplateRepository;
import st.old.isis.dto.analysis.MessageTemplateDto;
import st.old.isis.exception.IsisException;

@Component
@RequiredArgsConstructor
public class MessageTemplateValidator {

    final private MessageTemplateRepository repository;

    public void validate(@NonNull final MessageTemplateDto dto) throws IsisException {

        final var isTextTemplateEmpty = Strings.isEmpty(dto.getTextTemplate());

        if (isTextTemplateEmpty) {
            throw IsisException.badRequest("Text template is empty", "ValidationException");
        }

        final var isTypeEmpty = dto.getType() == null;

        if (isTypeEmpty) {
            throw IsisException.badRequest("Type is empty", "ValidationException");
        }
    }

    public void createValidate(@NonNull final MessageTemplateDto dto) throws IsisException {

        final var optTemplate = repository.findByName(dto.getName());

        if (optTemplate.isPresent()) {
            throw IsisException.conflict("Template name already exists", "ConflictException");
        }

        validate(dto);
    }

    public void updateValidate(@NonNull final MessageTemplateDto dto) throws IsisException {

        final var id = dto.getId();

        repository.findById(dto.getId())
                .orElseThrow(() -> IsisException.notFound(
                        String.format("Cannot find template with id: [%s]", id),
                        "NotFoundException"));

        validate(dto);
    }

}
