package me.urninax.flagdelivery.shared.security.providers;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.shared.security.principals.UserPrincipal;
import me.urninax.flagdelivery.shared.security.tokens.JwtAuthenticationToken;
import me.urninax.flagdelivery.shared.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider{
    private final JwtUtils jwtUtils;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException{
        String token = (String) authentication.getCredentials();

        UserPrincipal principal = jwtUtils.validate(token);

        return new JwtAuthenticationToken(principal, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication){
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
