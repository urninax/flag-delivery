package me.urninax.flagdelivery.organisation.utils.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException{
    public ForbiddenException(){
        super("Forbidden. Access to the resource was denied.", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
