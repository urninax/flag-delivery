package me.urninax.flagdelivery.user.ui.controllers.advice;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.user.utils.ErrorMessage;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice{
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorMessage> handleUnrecognizedProperties(HttpMessageNotReadableException exc, WebRequest request){
        Throwable cause = exc.getCause();

        String message = "Malformed request";

        if(cause instanceof UnrecognizedPropertyException unrecognizedPropertyExc){
            message = String.format("Unknown field: %s", unrecognizedPropertyExc.getPropertyName());
        }

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException exc,
                                                                   WebRequest request){
//        String message = "Unknown validation error";
//        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
//
//        StringBuilder stringBuilder = new StringBuilder();
//
//        exc.getBindingResult()
//                .getFieldErrors()
//                .forEach((field) -> stringBuilder
//                        .append(field.getDefaultMessage())
//                        .append("; "));
//
//        if(!stringBuilder.isEmpty()){
//            message = stringBuilder.toString();
//            status = HttpStatus.BAD_REQUEST;
//        }

        String message = exc.getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
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
}
