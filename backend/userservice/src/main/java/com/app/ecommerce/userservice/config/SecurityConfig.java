package com.app.ecommerce.userservice.config;

import com.app.ecommerce.userservice.security.jwt.JwtAuthenticationFilter;
import com.app.ecommerce.userservice.security.services.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AppUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AppUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/images/**", "/h2-console/**").permitAll()

                        .requestMatchers(
                                HttpMethod.POST, "/api/addresses"
                        ).hasAuthority("USER")

                        .requestMatchers(
                                HttpMethod.GET, "/api/addresses/me"
                        ).hasAuthority("USER")

                        .requestMatchers(
                                HttpMethod.PUT, "/api/addresses/**"
                        ).hasAuthority("USER")

                        .requestMatchers(
                                HttpMethod.DELETE, "/api/addresses/**"
                        ).hasAuthority("USER")

                        .requestMatchers(
                                HttpMethod.GET, "/api/addresses"
                        ).hasAuthority("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET, "/api/addresses/**"
                        ).hasAnyAuthority("ADMIN", "USER")

                        .requestMatchers("/api/**").authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}