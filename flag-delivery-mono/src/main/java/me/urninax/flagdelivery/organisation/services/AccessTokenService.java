package me.urninax.flagdelivery.organisation.services;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.AccessTokenRepository;
import me.urninax.flagdelivery.organisation.repositories.MembershipsRepository;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.user.security.principals.AccessTokenPrincipal;
import me.urninax.flagdelivery.user.utils.AccessTokenUtils;
import me.urninax.flagdelivery.user.utils.EntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessTokenService{
    private final AccessTokenRepository accessTokenRepository;
    private final MembershipsRepository membershipsRepository;
    private final EntityMapper entityMapper;

    public String issueToken(UUID userId, CreateAccessTokenRequest request){
        Membership membership = membershipsRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("No role in any organisation"));

        OrgRole role = membership.getRole();
        if(!role.atLeast(request.getRole())){
            throw new AccessDeniedException("Insufficient user role for token with requested role");
        }

        String token = String.format("api-%s", UUID.randomUUID());
        String hashedToken = AccessTokenUtils.hashSha256(token);
        String tokenHint = AccessTokenUtils.toHint(token);

        AccessToken accessTokenEntity = AccessToken.builder()
                .hashedToken(hashedToken)
                .tokenHint(tokenHint)
                .name(request.getName())
                .role(request.getRole())
                .isService(request.isService())
                .owner(membership.getUser()) //TODO: check if its better to do with User/Org from membership, or request proxies from respective repositories
                .organisation(membership.getOrganisation())
                .build();

        accessTokenRepository.save(accessTokenEntity);
        return token;
    }

    public Page<AccessTokenDTO> getTokensForUserInOrg(UUID userId, Pageable pageable, Optional<Boolean> showAllOptional){
        Membership membership = membershipsRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("No role in any organisation"));

        OrgRole role = membership.getRole();

        boolean showAll = showAllOptional.orElse(false);

        if(showAll){
            assertAdmin(role);

            Page<AccessToken> allTokensPage = accessTokenRepository
                    .findAllByOrganisation_Id(membership.getOrganisation().getId(), pageable);
            return allTokensPage.map(entityMapper::toDTO);
        }

        Page<AccessToken> allUserTokensPage = accessTokenRepository
                .findAllByOwner_IdAndOrganisation_Id(membership.getUserId(), membership.getOrganisation().getId(), pageable);

        return allUserTokensPage.map(entityMapper::toDTO);
    }

    public AccessTokenPrincipal validateAndResolve(String token){
        String hashedToken = AccessTokenUtils.hashSha256(token);

        AccessToken accessToken = accessTokenRepository.findByHashedToken(hashedToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid access token"));

        return AccessTokenPrincipal.builder()
                .ownerId(accessToken.getOwner().getId())
                .organisationId(accessToken.getOrganisation().getId())
                .authorities(List.of(
                        new SimpleGrantedAuthority(String.format("ROLE_ORG_%s", accessToken.getRole()))
                ))
                .build();
    }

    private void assertAdmin(OrgRole role){
        if(role != OrgRole.ADMIN){
            throw new AccessDeniedException("Role for this request is not sufficient");
        }
    }
}
