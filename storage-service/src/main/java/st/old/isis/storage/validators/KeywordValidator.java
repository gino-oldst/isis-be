package st.old.isis.storage.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.repositories.KeywordRepository;

@Component
@RequiredArgsConstructor
public class KeywordValidator {

    private final KeywordRepository repository;

    public void validate(@NonNull final KeywordDto dto) throws IsisException {

        final var optKeyword = repository.findByWord(dto.getWord());

        if (optKeyword.isPresent()) {
            throw IsisException.conflict("Keyword already exists", "ConflictException");
        }
    }
}
