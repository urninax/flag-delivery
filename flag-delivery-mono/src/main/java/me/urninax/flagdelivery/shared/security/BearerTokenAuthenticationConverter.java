package me.urninax.flagdelivery.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import me.urninax.flagdelivery.shared.security.tokens.AccessTokenAuthenticationToken;
import me.urninax.flagdelivery.shared.security.tokens.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class BearerTokenAuthenticationConverter implements AuthenticationConverter{
    @Override
    public Authentication convert(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return null;
        }

        String token = authHeader.substring(7);

        if(token.startsWith("api-")){
            return new AccessTokenAuthenticationToken(token);
        }else{
            return new JwtAuthenticationToken(token);
        }
    }
}
