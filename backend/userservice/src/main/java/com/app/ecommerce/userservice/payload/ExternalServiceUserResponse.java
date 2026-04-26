package com.app.ecommerce.userservice.payload;

import java.util.HashSet;
import java.util.Set;

public class ExternalServiceUserResponse {
    private Long userId;
    private String username;
    private String email;
    private String password;
    private Set<String> roles = new HashSet<>();
    private AddressDTO address;
    private Long cartId;

    public ExternalServiceUserResponse(Long userId, String username, String email, String password, Set<String> roles, AddressDTO address, Long cartId) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.address = address;
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
}
