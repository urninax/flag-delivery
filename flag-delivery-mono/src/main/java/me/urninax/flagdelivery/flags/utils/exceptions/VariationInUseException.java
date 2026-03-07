package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class VariationInUseException extends ApiException{
    public VariationInUseException(String message){
        super(message, HttpStatus.BAD_REQUEST, "VARIATION_IN_USE");
    }
}
