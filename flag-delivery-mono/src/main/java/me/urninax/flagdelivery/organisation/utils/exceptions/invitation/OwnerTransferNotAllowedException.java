package me.urninax.flagdelivery.organisation.utils.exceptions.invitation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class OwnerTransferNotAllowedException extends ApiException{
    public OwnerTransferNotAllowedException(){
        super("User is owner of the current organisation. Transfer is impossible.", HttpStatus.CONFLICT, "OWNER_TRANSFER_NOT_ALLOWED");
    }
}
