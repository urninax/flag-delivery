package me.urninax.flagdelivery.projectsenvs.utils.exceptions.project;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class SortNotPossibleException extends ApiException{
    public SortNotPossibleException(){
        super("Cannot be sorted", HttpStatus.BAD_REQUEST, "SORT_IMPOSSIBLE");
    }
}
