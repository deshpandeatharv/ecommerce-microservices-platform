package com.app.ecommerce.orderservice.util;

import com.app.ecommerce.orderservice.payload.PaymentDTO;
import com.app.ecommerce.orderservice.payload.StripePaymentDto;
import com.app.ecommerce.orderservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PaymentUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private final static String serviceUrl = "http://localhost:8084/api";

    public void savePayment(Long orderId, PaymentDTO paymentDTO) {

        try {
            String token = jwtUtils.extractToken();

            String url = serviceUrl + "/payments/" + orderId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<PaymentDTO> entity = new HttpEntity<>(paymentDTO, headers);

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Payment Service", e);
        }
    }

    public Map<String, String> createStripeClientSecret(StripePaymentDto dto) {

        try {
            String token = jwtUtils.extractToken();

            String url = serviceUrl + "/order/stripe-client-secret";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<StripePaymentDto> entity = new HttpEntity<>(dto, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Stripe PaymentIntent", e);
        }
    }
}
