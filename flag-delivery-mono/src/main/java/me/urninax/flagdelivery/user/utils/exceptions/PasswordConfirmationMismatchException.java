package me.urninax.flagdelivery.user.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class PasswordConfirmationMismatchException extends ApiException{
    public PasswordConfirmationMismatchException(){
        super("New password and confirmation do not match.", HttpStatus.BAD_REQUEST, "PASSWORD_CONFIRMATION_MISSMATCH");
    }
}
