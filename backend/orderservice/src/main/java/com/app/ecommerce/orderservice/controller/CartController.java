package com.app.ecommerce.orderservice.controller;

import com.app.ecommerce.orderservice.model.Cart;
import com.app.ecommerce.orderservice.payload.CartDTO;
import com.app.ecommerce.orderservice.payload.CartItemDTO;
import com.app.ecommerce.orderservice.payload.ExternalServiceCartResponse;
import com.app.ecommerce.orderservice.repository.CartRepository;
import com.app.ecommerce.orderservice.service.CartService;
import com.app.ecommerce.orderservice.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<String> createOrUpdateCart(
            @RequestBody List<CartItemDTO> cartItems) {

        String response = cartService.createOrUpdateCartWithItems(cartItems);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<CartDTO>> getCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @GetMapping("/myCart")
    public ResponseEntity<CartDTO> getMyCart() {

        String emailId = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);

        CartDTO cartDTO = cartService.getCart(emailId, cart.getCartId());
        return ResponseEntity.ok(cartDTO);
    }


    @PutMapping("/products/{productId}")
    public ResponseEntity<CartDTO> updateCartProduct(
            @PathVariable String productId,
            @RequestParam String operation) {

        CartDTO cartDTO = cartService.updateProductQuantityInCart(
                productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1
        );

        return ResponseEntity.ok(cartDTO);
    }

    @DeleteMapping("/{cartId}/products/{productId}")
    public ResponseEntity<CartDTO> deleteProductFromCart(
            @PathVariable Long cartId,
            @PathVariable String productId) {

        return ResponseEntity.ok(
                cartService.deleteProductFromCart(cartId, productId)
        );
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<List<ExternalServiceCartResponse>> getCartByProductId(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                cartService.getCartsByProductId(productId)
        );
    }
}
