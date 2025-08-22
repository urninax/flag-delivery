package me.urninax.flagdelivery.user.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.user.security.principals.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils{
    @Value("${token.secret}")
    private String tokenSecret;

    @Value("${token.expiration-time}")
    private long expirationTime;

    public String generateToken(String userId, String email, List<String> roles){
        byte[] secretBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = Keys.hmacShaKeyFor(secretBytes);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("roles", roles)
                .expiration(Date.from(Instant.now().plusMillis(expirationTime)))
                .issuedAt(Date.from(Instant.now()))
                .signWith(secretKey)
                .compact();
    }

    public UserPrincipal validate(String token){
        UserPrincipal principal;

        try{
            Claims claims = parse(token);

            String uuid = claims.getSubject();
            String email = claims.get("email", String.class);
            List<String> roles = claims.get("roles", List.class);

            List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();

            principal = UserPrincipal.builder()
                    .id(UUID.fromString(uuid))
                    .username(email)
                    .authorities(authorities)
                    .build();
        }catch(JwtException e){
            log.warn("JWT is invalid: {}",e.getLocalizedMessage());
            throw new BadCredentialsException("JWT is invalid");
        }

        return principal;
    }

    public Claims parse(String token) throws JwtException{
        byte[] secretBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = Keys.hmacShaKeyFor(secretBytes);

        JwtParser jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        Jws<Claims> parsedToken = jwtParser.parseSignedClaims(token);

        return parsedToken.getPayload();
    }
}
