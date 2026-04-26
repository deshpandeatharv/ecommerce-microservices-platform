package com.app.ecommerce.userservice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    // ⚠️ keep in env in real apps
    private final Key key =
            Keys.hmacShaKeyFor("replace-with-256-bit-secret-key-replace-with-256-bit".getBytes());

    @Value("${spring.app.jwtExpirationMs}")
    private long jwtValidityMs;

    public String generateToken(String username, Map<String, Object> claims) {

        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtValidityMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token).getBody();

            return !isTokenExpired(claims);

        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");
        } catch (SignatureException e) {
            System.out.println("Invalid signature");
        } catch (Exception e) {
            System.out.println("Invalid token");
        }

        return false;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public Long extractUserId(String token) {
        return parseClaims(token).getBody().get("userId", Long.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).getBody().get("role", String.class);
    }
}