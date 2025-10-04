package me.urninax.flagdelivery.organisation.utils.exceptions.membership;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class OwnerRoleModificationException extends ApiException{
    public OwnerRoleModificationException(){
        super("Owner role cannot be modified.", HttpStatus.FORBIDDEN, "OWNER_ROLE_MODIFICATION_FORBIDDEN");
    }
}
