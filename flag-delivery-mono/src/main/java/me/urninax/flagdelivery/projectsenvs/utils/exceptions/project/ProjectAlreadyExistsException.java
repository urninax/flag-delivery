package me.urninax.flagdelivery.projectsenvs.utils.exceptions.project;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class ProjectAlreadyExistsException extends ApiException{
    public ProjectAlreadyExistsException(){
        super("Project key already in use.", HttpStatus.CONFLICT, "PROJECT_ALREADY_EXISTS");
    }
}
