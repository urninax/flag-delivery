package me.urninax.flagdelivery.user.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class IncorrectCurrentPasswordException extends ApiException{
    public IncorrectCurrentPasswordException(){
        super("Incorrect provided current password", HttpStatus.BAD_REQUEST, "INCORRECT_CURRENT_PASSWORD");
    }
}
