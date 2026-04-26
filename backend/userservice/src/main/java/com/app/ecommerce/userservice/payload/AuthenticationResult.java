package com.app.ecommerce.userservice.payload;

import com.app.ecommerce.userservice.security.response.UserInfoResponse;
import jakarta.servlet.http.Cookie;

public class AuthenticationResult {
    private final UserInfoResponse response;
    private final Cookie jwtCookie;

    public AuthenticationResult(UserInfoResponse response, Cookie jwtCookie) {
        this.response = response;
        this.jwtCookie = jwtCookie;
    }

    public UserInfoResponse getResponse() {
        return response;
    }

    public Cookie getJwtCookie() {
        return jwtCookie;
    }
}
