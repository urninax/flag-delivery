package me.urninax.flagdelivery.user.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.urninax.flagdelivery.user.security.BearerTokenAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class BearerAuthenticationFilter extends OncePerRequestFilter{
    private final AuthenticationManager authenticationManager;
    private final BearerTokenAuthenticationConverter converter;

    public BearerAuthenticationFilter(AuthenticationManager authenticationManager, BearerTokenAuthenticationConverter converter){
        this.authenticationManager = authenticationManager;
        this.converter = converter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        Authentication authentication = converter.convert(request);

        if(authentication != null){
            try{
                Authentication result = authenticationManager.authenticate(authentication);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(result);

                SecurityContextHolder.setContext(context);
            }catch(AuthenticationException e){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getLocalizedMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
