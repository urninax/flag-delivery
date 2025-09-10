package me.urninax.flagdelivery.user.ui.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.shared.utils.ErrorMessage;
import me.urninax.flagdelivery.user.utils.exceptions.EmailAlreadyExistsException;
import me.urninax.flagdelivery.user.utils.exceptions.PasswordMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class UserControllerAdvice{
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
