package st.old.isis.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class IsisException extends Exception {
    private static final long serialVersionUID = 4940611221962196143L;

    private final String exception;

    private HttpStatus status;
    private List<String> exceptionParameters = new ArrayList<>();

    public IsisException(String exception) {
        this.exception = exception;
    }

    public IsisException(String message, String exception) {
        super(message);
        this.exception = exception;
    }

    public IsisException(HttpStatus status, String message, String exception) {
        super(message);
        this.status = status;
        this.exception = exception;
    }

    public IsisException(HttpStatus status, String message, String exception, List<String> exceptionParameters) {
        super(message);
        this.status = status;
        this.exception = exception;
        this.exceptionParameters = exceptionParameters;
    }

    public static IsisException exception(String message, String exception) {
        return new IsisException(message, exception);
    }

    public static IsisException badRequest(@NonNull final String message, @NonNull final String exception) {
        return new IsisException(HttpStatus.BAD_REQUEST, message, exception);
    }

    public static IsisException notFound(@NonNull final String message, @NonNull final String exception) {
        return new IsisException(HttpStatus.NOT_FOUND, message, exception);
    }

    public static IsisException internalServerError(@NonNull final String message, @NonNull final String exception) {
        return new IsisException(HttpStatus.INTERNAL_SERVER_ERROR, message, exception);
    }

    public static IsisException conflict(@NonNull final String message, @NonNull final String exception) {
        return new IsisException(HttpStatus.CONFLICT, message, exception);
    }
}
