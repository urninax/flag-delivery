package me.urninax.flagdelivery.organisation.utils.exceptions.invitation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvitationExpiredException extends ApiException{
    public InvitationExpiredException(){
        super("Invitation is expired.", HttpStatus.GONE, "INVITATION_EXPIRED");
    }
}
