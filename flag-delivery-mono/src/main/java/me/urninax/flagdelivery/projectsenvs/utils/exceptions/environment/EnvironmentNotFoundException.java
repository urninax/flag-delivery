package me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class EnvironmentNotFoundException extends ApiException{
    public EnvironmentNotFoundException(){
        super("Environment was not found", HttpStatus.NOT_FOUND, "ENV_NOT_FOUND");
    }
}
