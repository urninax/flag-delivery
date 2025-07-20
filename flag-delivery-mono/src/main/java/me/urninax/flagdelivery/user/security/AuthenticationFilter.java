package me.urninax.flagdelivery.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter{
    private final UsersServiceImpl usersService;
    private final Environment environment;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UsersServiceImpl usersService, Environment environment){
        super(authenticationManager);
        this.usersService = usersService;
        this.environment = environment;
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
        String tokenSecret = environment.getProperty("token.secret");
        byte[] secretBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = Keys.hmacShaKeyFor(secretBytes);

        String token = Jwts.builder()
                .subject(userEntity.getId().toString())
                .claim("email", email)
                .claim("roles", Collections.emptyList())
                .expiration(Date.from(Instant.now().plusMillis(Long.parseLong(Objects.requireNonNull(environment.getProperty("token.expiration-time"))))))
                .issuedAt(Date.from(Instant.now()))
                .signWith(secretKey)
                .compact();

        response.addHeader("Authorization", String.format("Bearer %s", token));
    }
}
