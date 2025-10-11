package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class FlagConfigAlreadyExistsException extends ApiException{
    public FlagConfigAlreadyExistsException(){
        super("Flag config(s) already exists.", HttpStatus.CONFLICT, "FLAG_CONFIG_ALREADY_EXISTS");
    }
}
