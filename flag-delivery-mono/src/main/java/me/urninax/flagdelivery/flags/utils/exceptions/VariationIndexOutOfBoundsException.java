package me.urninax.flagdelivery.flags.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class VariationIndexOutOfBoundsException extends ApiException{
    public VariationIndexOutOfBoundsException(){
        super("Default variation index is out of bounds.", HttpStatus.BAD_REQUEST, "VAR_INDEX_OUT_OF_BOUNDS");
    }
}
