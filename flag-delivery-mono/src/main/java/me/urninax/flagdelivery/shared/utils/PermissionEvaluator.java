package me.urninax.flagdelivery.shared.utils;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("perm")
public class PermissionEvaluator{

    private final CurrentUser currentUser;

    @Autowired
    public PermissionEvaluator(CurrentUser currentUser){
        this.currentUser = currentUser;
    }

    public boolean canAccess(OrgRole role){
        OrgRole userRole = currentUser.getOrgRole();
        return userRole.higherOrEqual(role);
    }
}
