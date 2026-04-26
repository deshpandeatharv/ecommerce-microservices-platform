package com.app.ecommerce.catalogservice.util;

import com.app.ecommerce.catalogservice.payload.ExternalServiceCartResponse;
import com.app.ecommerce.catalogservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class CartUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private static final String serviceUrl="http://localhost:8083/api";

    public void deleteProductFromCart(Long cartId, String productId) {

        String token = jwtUtils.extractToken();
        String url = serviceUrl + "/carts/" + cartId + "/product/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                String.class
        );
    }

    public void updateProductInCart(Long cartId, String productId) {
        String token = jwtUtils.extractToken();
        String url = serviceUrl + "/cart/products/" + productId + "/quantity/update";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                Void.class
        );
    }

    public List<ExternalServiceCartResponse> findCartsByProductId(String productId) {

        String token = jwtUtils.extractToken();
        String url = serviceUrl + "/carts/product/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // cleaner way

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ExternalServiceCartResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ExternalServiceCartResponse>>() {}
        );

        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }
}