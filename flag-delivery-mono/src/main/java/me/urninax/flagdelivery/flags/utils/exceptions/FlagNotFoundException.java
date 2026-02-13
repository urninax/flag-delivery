package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class FlagNotFoundException extends ApiException{
    public FlagNotFoundException(){
        super("Feature Flag not found.", HttpStatus.NOT_FOUND, "FLAG_NOT_FOUND");
    }
}
