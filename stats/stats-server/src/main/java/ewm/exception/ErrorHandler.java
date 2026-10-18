package ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    private static final String VALIDATION_ERROR = "Ошибка валидации";
    private static final String LOG_VALIDATION_ERROR = "Ошибка валидации: {}";
    private static final String LOG_INTERNAL_SERVER_ERROR = "Internal server error";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return String.format("Поле %s %s", fieldError.getField(), fieldError.getDefaultMessage());
                    }
                    return error.getDefaultMessage();
                })
                .orElse(VALIDATION_ERROR);

        log.warn(LOG_VALIDATION_ERROR, message);
        return new ErrorResponse(message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalError(Exception ex) {
        log.error(LOG_INTERNAL_SERVER_ERROR, ex);
        return new ErrorResponse(LOG_INTERNAL_SERVER_ERROR);
    }
}