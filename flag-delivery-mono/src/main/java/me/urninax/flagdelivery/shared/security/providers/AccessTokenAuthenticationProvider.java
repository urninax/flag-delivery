package me.urninax.flagdelivery.shared.security.providers;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import me.urninax.flagdelivery.shared.security.principals.AccessTokenPrincipal;
import me.urninax.flagdelivery.shared.security.tokens.AccessTokenAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenAuthenticationProvider implements AuthenticationProvider{
    private final AccessTokenService accessTokenService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException{
        String token = (String) authentication.getCredentials();

        AccessTokenPrincipal principal = accessTokenService.validateAndResolve(token);

        return new AccessTokenAuthenticationToken(principal, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication){
        return AccessTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
