package me.urninax.flagdelivery.organisation.utils.exceptions.accesstoken;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidAccessTokenException extends ApiException{
    public InvalidAccessTokenException(){
        super("Invalid access token.", HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN");
    }
}
