package com.example.wallet.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final String secret = "mi_clave_secreta_segura_que_debe_tener_minimo_32_caracteres";
    private final long expirationMillis = 3600000; // 1 hora

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String rol) {
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("rol", rol))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return getAllClaims(token).getSubject();
    }

    public String extractRol(String token) {
        return getAllClaims(token).get("rol", String.class);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}