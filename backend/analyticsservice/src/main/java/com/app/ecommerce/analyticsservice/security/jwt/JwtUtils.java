package com.app.ecommerce.analyticsservice.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private final Key key =
            Keys.hmacShaKeyFor("replace-with-256-bit-secret-key-replace-with-256-bit".getBytes());

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    @Autowired
    private HttpServletRequest request;

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

    public String extractToken() {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }


        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}