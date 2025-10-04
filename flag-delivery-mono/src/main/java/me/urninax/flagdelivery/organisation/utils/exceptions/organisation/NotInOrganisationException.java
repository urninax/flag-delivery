package me.urninax.flagdelivery.organisation.utils.exceptions.organisation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class NotInOrganisationException extends ApiException{
    public NotInOrganisationException(){
        super("User has no organisation.", HttpStatus.BAD_REQUEST, "HAS_NO_ORG");
    }
}
