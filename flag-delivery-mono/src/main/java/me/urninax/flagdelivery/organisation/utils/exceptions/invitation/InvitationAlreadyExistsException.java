package me.urninax.flagdelivery.organisation.utils.exceptions.invitation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvitationAlreadyExistsException extends ApiException {
    public InvitationAlreadyExistsException(){
        super("Invitation already exists.", HttpStatus.CONFLICT, "INV_EXISTS");
    }
}
