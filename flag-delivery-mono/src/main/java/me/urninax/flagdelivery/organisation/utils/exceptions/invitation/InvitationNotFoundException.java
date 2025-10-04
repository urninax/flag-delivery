package me.urninax.flagdelivery.organisation.utils.exceptions.invitation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvitationNotFoundException extends ApiException{
    public InvitationNotFoundException(){
        super("Invitation was not found.", HttpStatus.NOT_FOUND, "INVITATION_NOT_FOUND");
    }
}
