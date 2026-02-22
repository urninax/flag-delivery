package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class VariationNotFoundException extends ApiException{
    public VariationNotFoundException(){
        super("Flag variation was not found", HttpStatus.NOT_FOUND, "VARIATION_NOT_FOUND");
    }
}
