package me.urninax.flagdelivery.projectsenvs.utils.exceptions.environment;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class MissingEnvironmentException extends ApiException{
    public MissingEnvironmentException(){
        super("Project must have at least one environment", HttpStatus.BAD_REQUEST, "ENV_MISSING");
    }
}
