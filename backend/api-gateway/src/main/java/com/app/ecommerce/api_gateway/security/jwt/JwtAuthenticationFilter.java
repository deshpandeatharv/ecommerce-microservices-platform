package com.app.ecommerce.api_gateway.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Autowired
    private JwtUtils jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange);

        // If no token → block
        if (token == null || !jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract data
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String username = jwtUtil.extractUsername(token);

        // Pass headers to microservices
        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header("X-USER-ID", String.valueOf(userId))
                .header("X-USERNAME", username)
                .header("X-ROLE", role)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private String resolveToken(ServerWebExchange exchange) {

        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst("jwt");

        if (cookie != null) {
            return cookie.getValue();
        }

        String header = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}