package com.app.ecommerce.orderservice.payload;


import java.util.ArrayList;
import java.util.List;

public class ExternalServiceCartResponse {
    private Long cartId;
    private Long userId;

    private List<ExternalServiceCartItemResponse> externalServiceCartItems = new ArrayList<>();

    private Double totalPrice = 0.0;

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<ExternalServiceCartItemResponse> getExternalServiceCartItems() {
        return externalServiceCartItems;
    }

    public void setExternalServiceCartItems(List<ExternalServiceCartItemResponse> externalServiceCartItems) {
        this.externalServiceCartItems = externalServiceCartItems;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
