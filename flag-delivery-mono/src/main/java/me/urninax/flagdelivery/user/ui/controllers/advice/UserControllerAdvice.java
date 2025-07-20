package me.urninax.flagdelivery.user.ui.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.user.utils.ErrorMessage;
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
                status = HttpStatus.BAD_REQUEST;
                break;
            }
        }

        ErrorMessage errorMessage = ErrorMessage.builder()
                .path(request.getDescription(false).replace("uri=", ""))
                .message(message)
                .instant(Instant.now())
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }
}
