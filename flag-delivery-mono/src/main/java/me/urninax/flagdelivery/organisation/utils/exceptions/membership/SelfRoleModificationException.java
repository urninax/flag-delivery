package me.urninax.flagdelivery.organisation.utils.exceptions.membership;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class SelfRoleModificationException extends ApiException{
    public SelfRoleModificationException(){
        super("You cannot modify your own role.", HttpStatus.BAD_REQUEST, "SELF_ROLE_MODIFICATION");
    }
}
