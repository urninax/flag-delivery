package me.urninax.flagdelivery.user.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException{
    public UserNotFoundException(){
        super("User was not found.", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND");
    }
}
