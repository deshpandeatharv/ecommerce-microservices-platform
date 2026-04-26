package com.app.ecommerce.orderservice.util;

import com.app.ecommerce.orderservice.payload.ProductDTO;
import com.app.ecommerce.orderservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private final static String serviceUrl = "http://localhost:8082/api";

    public ProductDTO validateProduct(String productId) {

        try {
            String token = jwtUtils.extractToken();

            String url = serviceUrl + "/public/product/" + productId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ProductDTO.class
            );

            return response.getBody();

        } catch (Exception e) {
            return null; // product not found or service error
        }
    }

}
