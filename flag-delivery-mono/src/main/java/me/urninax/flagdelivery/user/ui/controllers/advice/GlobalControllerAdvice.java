package me.urninax.flagdelivery.user.ui.controllers.advice;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.user.utils.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

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
                .path(request.getDescription(false).replace("uri=", ""))
                .message(message)
                .instant(Instant.now())
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException exc,
                                                                   WebRequest request){
        String message = "Unknown validation error";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        StringBuilder stringBuilder = new StringBuilder();

        exc.getBindingResult()
                .getFieldErrors()
                .forEach((field) -> stringBuilder
//                        .append(field.getField())
//                        .append(": ")
                        .append(field.getDefaultMessage())
                        .append("; "));

        if(!stringBuilder.isEmpty()){
            message = stringBuilder.toString();
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorMessage errorMessage = ErrorMessage.builder()
                .path(request.getDescription(false).replace("uri=", ""))
                .message(message)
                .instant(Instant.now())
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }
}
