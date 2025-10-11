package me.urninax.flagdelivery.shared.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException{
    public ForbiddenException(){
        super("Forbidden. Access to the resource was denied.", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
