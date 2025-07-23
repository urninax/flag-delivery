package me.urninax.flagdelivery.organisation.services;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.AccessTokenRepository;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessTokenService{
    private final AccessTokenRepository accessTokenRepository;
    private final MembershipsRepository membershipsRepository;

    public String issueToken(UUID userId, CreateAccessTokenRequest request){
        Membership membership = membershipsRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("No role in this organisation"));

        OrgRole role = membership.getRole();
        if(!role.atLeast(request.role())){
            throw new AccessDeniedException("Insufficient user role for token with requested role");
        }

        AccessToken accessTokenEntity = AccessToken.builder()
                .name(request.name())
                .role(request.role())
                .isService(request.isService())
                .owner(membership.getUser()) //TODO: check if its better to do with User/Org from membership, or request proxies from respective repositories
                .organisation(membership.getOrganisation())
                .build();

        accessTokenRepository.save(accessTokenEntity);

        return String.format("api-%s", accessTokenEntity.getToken().toString());
    }
}
