package me.urninax.flagdelivery.user.security;

import me.urninax.flagdelivery.user.security.principals.AccessTokenPrincipal;
import me.urninax.flagdelivery.user.security.principals.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser{
    public UUID getUserId(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserPrincipal userPrincipal){
            return userPrincipal.getId();
        }

        if(principal instanceof AccessTokenPrincipal accessTokenPrincipal){
            return accessTokenPrincipal.getOwnerId();
        }

        throw new AccessDeniedException("Unknown principal type");
    }
}
