package com.app.ecommerce.api_gateway.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {

        return builder.routes()

                .route("userservice", r -> r
                        .path("/api/auth/**", "/api/addresses/**", "/api/users/**")
                        .uri("lb://USERSERVICE"))

                .route("catalogservice", r -> r
                        .path("/api/categories/**", "/api/products/**")
                        .uri("lb://CATALOGSERVICE"))

                .route("orderservice", r -> r
                        .path("/api/orders/**", "/api/carts/**")
                        .uri("lb://ORDERSERVICE"))

                .route("paymentservice", r -> r
                        .path("/api/payments/**")
                        .uri("lb://PAYMENTSERVICE"))

                .route("analyticsservice", r -> r
                        .path("/api/analytics/**")
                        .uri("lb://ANALYTICSSERVICE"))

                .build();
    }
}
