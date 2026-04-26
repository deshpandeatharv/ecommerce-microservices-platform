package com.app.ecommerce.orderservice.repository;

import com.app.ecommerce.orderservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import com.app.ecommerce.orderservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // ✅ FIND SINGLE CART ITEM
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.productId = ?2")
    CartItem findCartItemByProductIdAndCartId(Long cartId, Long productId);

    // ✅ DELETE SINGLE ITEM
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.productId = ?2")
    void deleteCartItemByProductIdAndCartId(Long cartId, Long productId);

    // ✅ DELETE ALL ITEMS IN CART
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = ?1")
    void deleteAllByCartId(Long cartId);

    // ✅ NEW METHOD (USED IN YOUR SERVICE)
    List<CartItem> findByProductId(Long productId);
}
