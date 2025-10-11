package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class FlagAlreadyExistsException extends ApiException{
    public FlagAlreadyExistsException(){
        super("Flag key already exists.", HttpStatus.CONFLICT, "FLAG_ALREADY_EXISTS");
    }
}
