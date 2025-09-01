package me.urninax.flagdelivery.organisation.ui.controllers.advice;

import me.urninax.flagdelivery.organisation.utils.exceptions.OrganisationAlreadyExistsException;
import me.urninax.flagdelivery.shared.utils.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class OrganisationsControllerAdvice{

    @ExceptionHandler({OrganisationAlreadyExistsException.class})
    public ResponseEntity<?> handleMessageException(Exception exc, WebRequest request){
        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .message(exc.getLocalizedMessage())
                .path(request.getDescription(false).substring(4))
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }
}
