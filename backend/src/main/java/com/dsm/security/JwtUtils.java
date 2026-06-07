package com.dsm.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        try {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("getSigningKey - Exception :", ex.getMessage());
            return null;
        }
    }
    public String generateToken(UserDetails userDetails) {
        try {
            return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
        } catch (Exception ex) {
            log.error("generateToken - Exception :", ex.getMessage());
            return null;
        }
    }
    public String extractUsername(String token) {
        try{
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("extractUsername - Exception :", ex.getMessage());
            return null;
        }
    }
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("validateToken - Exception", ex.getMessage());
            return false;
        }
    }
    private boolean isTokenExpired(String token) {
        try{
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("isTokenExpired - Exception :", ex.getMessage());
            return false;
        }
    }

}
