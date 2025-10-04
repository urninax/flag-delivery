package me.urninax.flagdelivery.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException{
    private final HttpStatus status;
    private final String errorCode;

    public ApiException(String message, HttpStatus status, String errorCode){
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
