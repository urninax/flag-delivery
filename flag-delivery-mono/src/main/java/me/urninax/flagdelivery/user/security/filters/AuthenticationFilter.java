package me.urninax.flagdelivery.user.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.*;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter{
    private final UsersServiceImpl usersService;
    private final JwtUtils jwtUtils;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UsersServiceImpl usersService, JwtUtils jwtUtils){
        super(authenticationManager);
        this.usersService = usersService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException{
        try{
            SigninRequest signinRequest = new ObjectMapper().readValue(request.getInputStream(), SigninRequest.class);

            return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getEmail(), signinRequest.getPassword(), new ArrayList<>()));
        }catch(IOException e){
            throw new AuthenticationServiceException("Failed to read request body", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException{
        String email = ((User) authResult.getPrincipal()).getUsername();
        UserEntity userEntity = usersService.findUserByEmail(email);

        List<String> stringAuthorities = userEntity
                .getInternalRoles()
                .stream().map(Enum::name)
                .toList();

        String token = jwtUtils.generateToken(userEntity.getId().toString(), email, stringAuthorities);

        response.addHeader("Authorization", String.format("Bearer %s", token));
    }
}
