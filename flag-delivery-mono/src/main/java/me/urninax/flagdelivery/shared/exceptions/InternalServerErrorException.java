package me.urninax.flagdelivery.shared.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends ApiException{
    public InternalServerErrorException(){
        super("", HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }
}
