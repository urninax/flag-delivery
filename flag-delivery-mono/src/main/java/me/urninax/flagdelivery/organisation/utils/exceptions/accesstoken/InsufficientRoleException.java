package me.urninax.flagdelivery.organisation.utils.exceptions.accesstoken;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InsufficientRoleException extends ApiException {
    public InsufficientRoleException() {
        super("Your organisation role is not sufficient for this requets.", HttpStatus.FORBIDDEN, "INSUFFICIENT_ROLE");
    }
}
