package me.urninax.flagdelivery.shared.security.providers;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import me.urninax.flagdelivery.organisation.shared.AccessTokenPrincipalDTO;
import me.urninax.flagdelivery.shared.security.principals.AccessTokenPrincipal;
import me.urninax.flagdelivery.shared.security.tokens.AccessTokenAuthenticationToken;
import me.urninax.flagdelivery.shared.utils.AccessTokenUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccessTokenAuthenticationProvider implements AuthenticationProvider{
    private final AccessTokenService accessTokenService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException{
        String token = (String) authentication.getCredentials();

        String hashedToken = AccessTokenUtils.hashSha256(token);
        AccessTokenPrincipalDTO tokenPrincipalDTO = accessTokenService.validateAndResolveByHash(hashedToken);

        AccessTokenPrincipal principal = AccessTokenPrincipal.builder()
                .ownerId(tokenPrincipalDTO.ownerId())
                .organisationId(tokenPrincipalDTO.organisationId())
                .authorities(List.of(
                            new SimpleGrantedAuthority(String.format("ROLE_ORG_%s", tokenPrincipalDTO.role()))
                )).build();

        return new AccessTokenAuthenticationToken(principal, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication){
        return AccessTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
