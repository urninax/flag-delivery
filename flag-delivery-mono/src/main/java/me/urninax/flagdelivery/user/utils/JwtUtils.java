package me.urninax.flagdelivery.user.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
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

    public Claims validateToken(String token) throws JwtException{
        byte[] secretBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = Keys.hmacShaKeyFor(secretBytes);

        JwtParser jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        Jws<Claims> parsedToken = jwtParser.parseSignedClaims(token);

        return parsedToken.getPayload();
    }
}
