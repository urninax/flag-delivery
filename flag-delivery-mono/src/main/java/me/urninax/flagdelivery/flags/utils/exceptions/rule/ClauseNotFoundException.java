package me.urninax.flagdelivery.flags.utils.exceptions.rule;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class ClauseNotFoundException extends ApiException{
    public ClauseNotFoundException(){
        super("Clause was not found", HttpStatus.NOT_FOUND, "CLAUSE_NOT_FOUND");
    }
}
