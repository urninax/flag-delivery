package me.urninax.flagdelivery.shared.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException{
    public BadRequestException(String message){
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
