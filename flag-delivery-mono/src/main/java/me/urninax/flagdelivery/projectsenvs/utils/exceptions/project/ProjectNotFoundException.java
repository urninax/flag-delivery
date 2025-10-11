package me.urninax.flagdelivery.projectsenvs.utils.exceptions.project;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends ApiException{
    public ProjectNotFoundException(){
        super("Project was not found.", HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND");
    }
}
