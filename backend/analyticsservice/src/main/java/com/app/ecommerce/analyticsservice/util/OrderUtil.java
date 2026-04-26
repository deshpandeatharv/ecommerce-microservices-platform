package com.app.ecommerce.analyticsservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private static final String serviceUrl="http://localhost:8083/api";

    public Long getOrderCount() {

        try {
            String token = jwtUtils.extractToken();

            String url = serviceUrl + "/admin/orders/count";

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
            throw new RuntimeException("Failed to fetch order count", e);
        }
    }

    // ✅ Get total revenue
    public Double getTotalRevenue() {

        try {
            String token = jwtUtils.extractToken();

            String url = serviceUrl + "/admin/orders/revenue";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Double> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Double.class
            );

            return response.getBody() != null ? response.getBody() : 0.0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch revenue", e);
        }
    }
}
