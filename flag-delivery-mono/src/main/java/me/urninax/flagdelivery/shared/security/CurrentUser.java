package me.urninax.flagdelivery.shared.security;

import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.security.principals.AccessTokenPrincipal;
import me.urninax.flagdelivery.shared.security.principals.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component("user")
public class CurrentUser{
    private final MembershipsRepository membershipsRepository;

    public CurrentUser(MembershipsRepository membershipsRepository){
        this.membershipsRepository = membershipsRepository;
    }

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

    public UUID getOrganisationId(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserPrincipal userPrincipal){
            Membership membership = membershipsRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no organisation"));

            return membership.getOrganisation().getId();
        }

        if(principal instanceof  AccessTokenPrincipal accessTokenPrincipal){
            return accessTokenPrincipal.getOrganisationId();
        }

        throw new AccessDeniedException("Unknown principal type");
    }

    public OrgRole getOrgRole(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserPrincipal userPrincipal){
            Membership membership = membershipsRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no organisation")); // cache in the future

            return membership.getRole();
        }

        if(principal instanceof AccessTokenPrincipal accessTokenPrincipal){
            return accessTokenPrincipal.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> OrgRole.valueOf(role.replace("ROLE_ORG_", "")))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        throw new AccessDeniedException("Unknown principal type");
    }

    public boolean isAuthMethod(AuthMethod authMethod){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserPrincipal){
            return authMethod == AuthMethod.JWT;
        }

        if(principal instanceof AccessTokenPrincipal){
            return authMethod == AuthMethod.ACCESS_TOKEN;
        }

        return false;
    }
}
