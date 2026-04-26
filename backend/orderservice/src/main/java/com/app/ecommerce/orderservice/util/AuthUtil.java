package com.app.ecommerce.orderservice.util;

import com.app.ecommerce.catalogservice.payload.ExternalServiceUserResponse;
import com.app.ecommerce.catalogservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private final static String serviceUrl = "http://localhost:8081/api/auth/user";

    public String loggedInEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExternalServiceUserResponse externalServiceUserResponse = validateUser();
        if(externalServiceUserResponse==null){
            throw new UsernameNotFoundException("User Not Found with username: " + authentication.getName());
        }

        return externalServiceUserResponse.getEmail();
    }

    public Long loggedInUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExternalServiceUserResponse externalServiceUserResponse = validateUser();
        if(externalServiceUserResponse==null){
            throw new UsernameNotFoundException("User Not Found with username: " + authentication.getName());
        }
        return externalServiceUserResponse.getUserId();
    }

    public ExternalServiceUserResponse loggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExternalServiceUserResponse externalServiceUserResponse = validateUser();
        if(externalServiceUserResponse==null){
            throw new UsernameNotFoundException("User Not Found with username: " + authentication.getName());
        }
        return externalServiceUserResponse;

    }

    public ExternalServiceUserResponse validateUser(){
        try {
            String token = jwtUtils.extractToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ExternalServiceUserResponse> response = restTemplate.exchange(
                    serviceUrl,
                    HttpMethod.GET,
                    entity,
                    ExternalServiceUserResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            return null; // user not found or service error
        }
    }

}