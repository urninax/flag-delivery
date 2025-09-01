package me.urninax.flagdelivery.user.ui.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.shared.utils.ErrorMessage;
import me.urninax.flagdelivery.user.utils.exceptions.EmailAlreadyExistsException;
import me.urninax.flagdelivery.user.utils.exceptions.PasswordMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class UserControllerAdvice{
    private static final Map<String, String> constraintMessages = Map.of(
            "unique_email", "Email already exists."
    );


    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorMessage> handleDataIntegrityViolationException(DataIntegrityViolationException exc,
                                                                   WebRequest request){
        String message = "Unknown persistence error";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(exc.getLocalizedMessage());

        for(var entry : constraintMessages.entrySet()){
            if(exc.getLocalizedMessage().contains(entry.getKey())){
                message = entry.getValue();
                status = HttpStatus.CONFLICT;
                break;
            }
        }

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, PasswordMismatchException.class})
    public ResponseEntity<ErrorMessage> handleEmailAlreadyExistsException(Exception exc,
                                                                              WebRequest request){

        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .message(exc.getLocalizedMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }
}
