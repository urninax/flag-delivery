package me.urninax.flagdelivery.projectsenvs.utils.exceptions.project;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPrefixException extends ApiException{
    public InvalidPrefixException(String message){
        super(message, HttpStatus.BAD_REQUEST, "INVALID_PREFIX");
    }
}
