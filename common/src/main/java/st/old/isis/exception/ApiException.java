package st.old.isis.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class ApiException {

    private final HttpStatus status;
    private final String message;
    private final List<String> errors;
    private String exception;

    public ApiException(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiException(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = List.of(error);
    }

    public ApiException(HttpStatus status, String message, String error, String exception) {
        super();
        this.status = status;
        this.message = message;
        errors = List.of(error);
        this.exception = exception;
    }
}

