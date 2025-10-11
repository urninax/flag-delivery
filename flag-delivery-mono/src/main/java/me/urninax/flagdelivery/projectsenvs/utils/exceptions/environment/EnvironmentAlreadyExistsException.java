package me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class EnvironmentAlreadyExistsException extends ApiException{
    public EnvironmentAlreadyExistsException(){
        super("Environment key already in use.", HttpStatus.CONFLICT, "ENV_ALREADY_EXISTS");
    }
}
