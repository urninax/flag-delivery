package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class VariationNotUniqueException extends ApiException{
    public VariationNotUniqueException(){
        super("Variations are not unique", HttpStatus.CONFLICT, "VAR_NOT_UNIQUE");
    }
}
