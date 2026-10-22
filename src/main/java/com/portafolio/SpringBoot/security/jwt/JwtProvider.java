package com.portafolio.SpringBoot.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    private final SecretKey key;
    private final long expirationMillis;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") int expirationSeconds) {
        byte[] bytes = Decoders.BASE64.decode(secret);
        if (bytes.length < 64 || expirationSeconds <= 0) {
            throw new IllegalArgumentException("JWT_SECRET debe contener al menos 64 bytes aleatorios en Base64 y la duraciÃ³n debe ser positiva");
        }
        key = Keys.hmacShaKeyFor(bytes);
        expirationMillis = Math.multiplyExact((long) expirationSeconds, 1000L);
    }

    public String generateToken(Authentication authentication) {
        Date now = new Date();
        return Jwts.builder().subject(authentication.getName()).issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(key, Jwts.SIG.HS512).compact();
    }

    public String getNombreUsuarioFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            return getNombreUsuarioFromToken(token) != null;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
