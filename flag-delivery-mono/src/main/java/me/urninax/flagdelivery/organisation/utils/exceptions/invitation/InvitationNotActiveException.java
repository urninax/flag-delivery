package me.urninax.flagdelivery.organisation.utils.exceptions.invitation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvitationNotActiveException extends ApiException{
    public InvitationNotActiveException(){
        super("Invitation is not active.", HttpStatus.GONE, "INVITATION_NOT_ACTIVE");
    }
}
