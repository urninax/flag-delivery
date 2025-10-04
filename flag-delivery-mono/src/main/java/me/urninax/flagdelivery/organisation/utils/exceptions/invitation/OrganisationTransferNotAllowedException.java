package me.urninax.flagdelivery.organisation.utils.exceptions.invitation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class OrganisationTransferNotAllowedException extends ApiException{
    public OrganisationTransferNotAllowedException(){
        super("User has an organisation. Transfer was not allowed.", HttpStatus.CONFLICT, "ORG_TRANSFER_NOT_ALLOWED");
    }
}
