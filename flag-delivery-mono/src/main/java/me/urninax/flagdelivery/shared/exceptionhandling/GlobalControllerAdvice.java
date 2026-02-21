package me.urninax.flagdelivery.shared.exceptionhandling;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.shared.exceptions.ApiException;
import me.urninax.flagdelivery.shared.exceptions.ConflictException;
import me.urninax.flagdelivery.shared.utils.ErrorMessage;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice{
    private final Clock clock;

    public GlobalControllerAdvice(Clock clock){
        this.clock = clock;
    }

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<ErrorMessage> handleApiException(ApiException exc, WebRequest request){
        HttpStatus status = exc.getStatus();

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(exc.getStatus().value())
                .errorCode(exc.getErrorCode())
                .message(exc.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorMessage> handleUnrecognizedProperties(HttpMessageNotReadableException exc, WebRequest request){
        Throwable cause = exc.getCause();

        String message = "Malformed request";
        String errorCode = "MALFORMED_REQUEST";

        if(cause instanceof UnrecognizedPropertyException unrecognizedPropertyExc){
            message = String.format("Unknown field: %s", unrecognizedPropertyExc.getPropertyName());
            errorCode = "UNKNOWN_FIELD";
        }else if(cause instanceof InvalidFormatException invalidFormatExc){
            Class<?> targetType = invalidFormatExc.getTargetType();
            Object value = invalidFormatExc.getValue();

            if(targetType.isEnum()){
                String allowed = Arrays.stream(targetType.getEnumConstants())
                        .map(e -> ((Enum<?>) e).name())
                        .collect(Collectors.joining(", "));

                message = String.format(
                        "Invalid value '%s' for field '%s'. Allowed values: [%s]",
                        value,
                        invalidFormatExc.getPath().isEmpty() ? "unknown" : invalidFormatExc.getPath().getFirst().getFieldName(),
                        allowed);

                errorCode = "INVALID_VALUE";
            }
        }else{
            if(exc.getMessage().contains("Required request body is missing:")){
                errorCode = "REQUIRED_FIELD_MISSING";
            }

        }

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .errorCode(errorCode)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException exc,
                                                                   WebRequest request){
        String message = exc.getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage) //todo: fix message for failed String to LocalDate binding (possible solution: take message from messages.properties)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("; "));

        HttpStatus status = message.isBlank() ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.BAD_REQUEST;

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .message(message.isBlank() ? "Unknown validation error" : message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }

    @ExceptionHandler({OptimisticLockException.class})
    public ResponseEntity<?> handleOptimisticLockException(WebRequest request){
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .message("Object was modified concurrently. Please retry")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }

    @ExceptionHandler({ConflictException.class})
    public ResponseEntity<?> handleConflictException(ConflictException exc, WebRequest request){
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now(clock))
                .status(status.value())
                .message(exc.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorMessage> handleDataIntegrityViolationException(DataIntegrityViolationException exc,
                                                                              WebRequest request){
        String message = "Unknown persistence error";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(exc.getLocalizedMessage());

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }
}
