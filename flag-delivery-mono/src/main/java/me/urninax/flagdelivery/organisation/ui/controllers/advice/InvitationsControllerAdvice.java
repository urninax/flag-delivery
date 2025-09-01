package me.urninax.flagdelivery.organisation.ui.controllers.advice;

import me.urninax.flagdelivery.organisation.utils.exceptions.InvitationNotFoundException;
import me.urninax.flagdelivery.shared.utils.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class InvitationsControllerAdvice{

    @ExceptionHandler({InvitationNotFoundException.class})
    public ResponseEntity<?> handleInvitationNotFound(InvitationNotFoundException exc, WebRequest request){
        ErrorMessage errorMessage = ErrorMessage.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .message(exc.getLocalizedMessage())
                .path(request.getDescription(false).substring(4))
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
}
