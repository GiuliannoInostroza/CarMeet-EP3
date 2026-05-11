package com.carmeet.ms_auth_user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;

    // ⏱ tiempos de expiración
    private final long EXPIRATION_MS = 1000 * 60 * 60;           // 1 hora
    private final long REFRESH_EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 horas

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 🔐 GENERAR ACCESS TOKEN
    public String generarToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔄 GENERAR REFRESH TOKEN
    public String generarRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ VALIDAR TOKEN
    public boolean esValido(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // 👤 OBTENER USUARIO
    public String obtenerUsuario(String token) {
        return getClaims(token).getSubject();
    }

    // 🔐 OBTENER ROLE
    public String obtenerRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 🔄 VALIDAR SI ES REFRESH TOKEN
    public boolean esRefreshToken(String token) {
        return "refresh".equals(getClaims(token).get("type"));
    }

    // 🔍 EXTRAER CLAIMS
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
