package com.app.ecommerce.analyticsservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CatalogUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private static final String serviceUrl="http://localhost:8082/api";

    public Long getProductCount() {

        try {
            String token = jwtUtils.extractToken();

            String url = serviceUrl + "/admin/products/count";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Long> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Long.class
            );

            return response.getBody() != null ? response.getBody() : 0L;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch product count from Catalog Service", e);
        }
    }
}
