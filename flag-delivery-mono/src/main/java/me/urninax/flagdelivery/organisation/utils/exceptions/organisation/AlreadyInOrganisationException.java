package me.urninax.flagdelivery.organisation.utils.exceptions.organisation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class AlreadyInOrganisationException extends ApiException{
    public AlreadyInOrganisationException(){
        super("User is already in organisation.", HttpStatus.CONFLICT, "ALREADY_IN_ORG");
    }
}
