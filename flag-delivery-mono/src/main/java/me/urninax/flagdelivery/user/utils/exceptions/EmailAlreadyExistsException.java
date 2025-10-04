package me.urninax.flagdelivery.user.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException{
    public EmailAlreadyExistsException(){
        super("Email already exists.", HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }
}
