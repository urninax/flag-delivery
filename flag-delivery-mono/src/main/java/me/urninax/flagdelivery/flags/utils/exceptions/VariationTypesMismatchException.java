package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class VariationTypesMismatchException extends ApiException{
    public VariationTypesMismatchException(){
        super("Variation types are different.", HttpStatus.BAD_REQUEST, "VAR_TYPES_MISMATCH");
    }
}
