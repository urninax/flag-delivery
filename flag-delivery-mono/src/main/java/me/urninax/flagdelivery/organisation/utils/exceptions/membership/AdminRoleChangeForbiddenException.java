package me.urninax.flagdelivery.organisation.utils.exceptions.membership;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class AdminRoleChangeForbiddenException extends ApiException{
    public AdminRoleChangeForbiddenException(){
        super("Only organisation owner can change ADMIN's role.", HttpStatus.FORBIDDEN, "ADMIN_ROLE_CHANGE_FORBIDDEN");
    }
}
