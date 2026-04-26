package com.app.ecommerce.orderservice.config;

import com.app.ecommerce.orderservice.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ========================
                        // ADMIN ONLY APIs
                        // ========================
                        .requestMatchers(
                                "/api/orders",
                                "/api/orders/count",
                                "/api/orders/revenue",
                                "/api/orders/*/status"
                        ).hasAuthority("ADMIN")

                        // ========================
                        // SELLER APIs
                        // ========================
                        .requestMatchers(
                                "/api/orders/seller",
                                "/api/orders/seller/**"
                        ).hasAuthority("SELLER")

                        // ========================
                        // USER APIs (place order, carts)
                        // ========================
                        .requestMatchers(
                                "/api/orders/**",
                                "/api/carts/**"
                        ).hasAuthority("USER")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}