package com.app.ecommerce.orderservice.util;

import com.app.ecommerce.orderservice.payload.AddressDTO;
import com.app.ecommerce.orderservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AddressUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private final static String serviceUrl = "http://localhost:8081/api";

    public AddressDTO getAddressById(Long addressId) {

        try {
            String token = jwtUtils.extractToken();

            String url = serviceUrl + "/addresses/" + addressId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<AddressDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AddressDTO.class
            );

            return response.getBody();

        } catch (Exception e) {
            return null; // address not found or service down
        }
    }
}