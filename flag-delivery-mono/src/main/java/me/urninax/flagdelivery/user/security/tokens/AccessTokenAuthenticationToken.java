package me.urninax.flagdelivery.user.security.tokens;

import me.urninax.flagdelivery.user.security.principals.AccessTokenPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AccessTokenAuthenticationToken extends AbstractAuthenticationToken{
    private String token;
    private final AccessTokenPrincipal principal;

    public AccessTokenAuthenticationToken(String token){
        super(null);
        this.token = token;
        this.principal = null;
        setAuthenticated(false);
    }

    public AccessTokenAuthenticationToken(AccessTokenPrincipal principal, Collection<? extends GrantedAuthority> authorities){
        super(authorities);
        this.token = null;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials(){
        return token;
    }

    @Override
    public Object getPrincipal(){
        return principal;
    }

    @Override
    public void eraseCredentials(){
        super.eraseCredentials();
        this.token = null;
    }
}
