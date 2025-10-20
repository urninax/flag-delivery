package me.urninax.flagdelivery.organisation.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.AccessTokenRepository;
import me.urninax.flagdelivery.organisation.services.caching.MemberTokensCacheService;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.shared.AccessTokenPrincipalDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.utils.exceptions.accesstoken.InsufficientRoleException;
import me.urninax.flagdelivery.organisation.utils.exceptions.accesstoken.InvalidAccessTokenException;
import me.urninax.flagdelivery.shared.exceptions.ForbiddenException;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.AccessTokenUtils;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessTokenService{
    private final AccessTokenRepository accessTokenRepository;
    private final MemberTokensCacheService memberTokensCacheService;
    private final EntityMapper entityMapper;
    private final CurrentUser currentUser;
    private final EntityManager em;

    @Transactional
    public String issueToken(CreateAccessTokenRequest request){
        UUID userId = currentUser.getUserId();
        UUID orgId = currentUser.getOrganisationId();

        UserEntity userRef = em.getReference(UserEntity.class, userId);
        Organisation orgRef = em.getReference(Organisation.class, orgId);

        if(request.getRole().higherThan(currentUser.getOrgRole())){
            throw new InsufficientRoleException();
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
                .owner(userRef)
                .organisation(orgRef)
                .build();

        accessTokenRepository.save(accessTokenEntity);
        return token;
    }

    public Page<AccessTokenDTO> getTokensForUserInOrg(Pageable pageable, Optional<Boolean> showAllOptional){
        //todo: review visibility semantics
        UUID userId = currentUser.getUserId();
        UUID orgId = currentUser.getOrganisationId();
        OrgRole role = currentUser.getOrgRole();

        boolean showAll = showAllOptional.orElse(false);

        if(showAll){
            assertAdmin(role);

            Page<AccessToken> allTokensPage = accessTokenRepository
                    .findAllByOrganisation_Id(orgId, pageable);
            return allTokensPage.map(entityMapper::toDTO);
        }

        Page<AccessToken> allUserTokensPage = accessTokenRepository
                .findAllByOwner_IdAndOrganisation_Id(userId, orgId, pageable);

        return allUserTokensPage.map(entityMapper::toDTO);
    }

    @Cacheable(value = "accessTokens", key = "#hashedToken")
    public AccessTokenPrincipalDTO validateAndResolveByHash(String hashedToken){
        AccessTokenPrincipalDTO accessTokenPrincipalDTO = accessTokenRepository.findByHashedToken(hashedToken)
                .orElseThrow(InvalidAccessTokenException::new);

        memberTokensCacheService.addToken(accessTokenPrincipalDTO.ownerId(), hashedToken);

        return accessTokenPrincipalDTO;
    }

    public void downgradeMemberTokens(UUID memberId, OrgRole role){
        accessTokenRepository.downgradeUserTokens(memberId, role);

        memberTokensCacheService.evictAllMemberTokens(memberId);
    }

    private void assertAdmin(OrgRole role){
        if(role.lowerThan(OrgRole.ADMIN)){
            throw new ForbiddenException();
        }
    }
}
