package com.app.ecommerce.orderservice.service;

import com.app.ecommerce.orderservice.exceptions.APIException;
import com.app.ecommerce.orderservice.exceptions.ResourceNotFoundException;
import com.app.ecommerce.orderservice.model.Cart;
import com.app.ecommerce.orderservice.model.CartItem;
import com.app.ecommerce.orderservice.payload.*;
import com.app.ecommerce.orderservice.repository.CartItemRepository;
import com.app.ecommerce.orderservice.repository.CartRepository;
import com.app.ecommerce.orderservice.util.AuthUtil;
import com.app.ecommerce.orderservice.util.CatalogUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartService{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CatalogUtil catalogUtil;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    public CartDTO addProductToCart(String productId, Integer quantity) {

        Cart cart = createCart();

        // ✅ Get product from Catalog Service
        ProductDTO product = catalogUtil.validateProduct(productId);

        if (product == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        // check already in cart
        CartItem cartItem = cartItemRepository
                .findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        // stock validation
        if (product.getQuantity() == null || product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please order less than or equal to available quantity: "
                    + product.getQuantity());
        }

        // create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setProductId(product.getProductId());
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepository.save(newCartItem);

        // update total price
        double itemTotal = product.getSpecialPrice() * quantity;
        cart.setTotalPrice(cart.getTotalPrice() + itemTotal);

        cartRepository.save(cart);

        // map response
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> products = cart.getCartItems()
                .stream()
                .map(item -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setProductId(item.getProductId());
                    dto.setQuantity(item.getQuantity());
                    dto.setDiscount(item.getDiscount());
                    dto.setSpecialPrice(item.getProductPrice());
                    return dto;
                })
                .toList();

        cartDTO.setProducts(products);

        return cartDTO;
    }

    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.size() == 0) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems()
                    .stream()
                    .map(cartItem -> {
                        ProductDTO productDTO = catalogUtil.validateProduct(cartItem.getProductId());

                        if (productDTO == null) {
                            return null;
                        }
                        productDTO.setQuantity(cartItem.getQuantity());

                        return productDTO;
                    })
                    .filter(p -> p != null)
                    .toList();


            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        return cartDTOs;
    }

    public CartDTO getCart(String emailId, Long cartId) {

        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> products = cart.getCartItems()
                .stream()
                .map(cartItem -> {

                    // 🔥 Fetch product from Catalog Service
                    ProductDTO productDTO =
                            catalogUtil.validateProduct(cartItem.getProductId());

                    if (productDTO == null) {
                        return null;
                    }

                    // attach cart-specific quantity
                    productDTO.setQuantity(cartItem.getQuantity());

                    return productDTO;
                })
                .filter(p -> p != null)
                .toList();

        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Transactional
    public CartDTO updateProductQuantityInCart(String productId, Integer quantity) {

        String emailId = authUtil.loggedInEmail();

        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        ProductDTO product = catalogUtil.validateProduct(productId);

        if (product == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please order <= " + product.getQuantity());
        }

        CartItem cartItem = cartItemRepository
                .findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product not in cart");
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if (newQuantity < 0) {
            throw new APIException("Quantity cannot be negative");
        }

        if (newQuantity == 0) {
            return deleteProductFromCart(cartId, productId);
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());

        cartItemRepository.save(cartItem);

        // update total price
        cart.setTotalPrice(
                cart.getTotalPrice() + (product.getSpecialPrice() * quantity)
        );

        cartRepository.save(cart);

        return buildCartDTO(cart);
    }


    private Cart createCart() {
        Cart userCart  = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUserId(authUtil.loggedInUserId());
        Cart newCart =  cartRepository.save(cart);

        return newCart;
    }


    @Transactional
    public CartDTO deleteProductFromCart(Long cartId, String productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository
                .findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(
                cart.getTotalPrice() -
                        (cartItem.getProductPrice() * cartItem.getQuantity())
        );

        cartItemRepository.delete(cartItem);

        return buildCartDTO(cart);
    }


    public void updateProductInCarts(Long cartId, String productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        ProductDTO product = catalogUtil.validateProduct(productId);

        if (product == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        CartItem cartItem = cartItemRepository
                .findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product not available in cart");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepository.save(cartItem);

        cart.setTotalPrice(
                cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity())
        );

        cartRepository.save(cart);
    }

    @Transactional
    public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {

        String emailId = authUtil.loggedInEmail();

        Cart existingCart = cartRepository.findCartByEmail(emailId);

        if (existingCart == null) {
            existingCart = new Cart();
            existingCart.setTotalPrice(0.00);
            existingCart.setUserId(authUtil.loggedInUserId());
            existingCart = cartRepository.save(existingCart);
        } else {
            cartItemRepository.deleteAllByCartId(existingCart.getCartId());
        }

        double totalPrice = 0.00;

        for (CartItemDTO cartItemDTO : cartItems) {

            Long productId = cartItemDTO.getProductId();
            Integer quantity = cartItemDTO.getQuantity();

            // 🔥 CALL CATALOG SERVICE
            ProductDTO product = catalogUtil.validateProduct(productId);

            if (product == null) {
                throw new ResourceNotFoundException("Product", "productId", productId);
            }

            totalPrice += product.getSpecialPrice() * quantity;

            CartItem cartItem = new CartItem();
            cartItem.setProductId(product.getProductId()); // IMPORTANT
            cartItem.setCart(existingCart);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());

            cartItemRepository.save(cartItem);
        }

        existingCart.setTotalPrice(totalPrice);
        cartRepository.save(existingCart);

        return "Cart created/updated successfully";
    }

    private CartDTO buildCartDTO(Cart cart) {

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> products = cart.getCartItems()
                .stream()
                .map(item -> {
                    ProductDTO product = catalogUtil.validateProduct(item.getProductId());
                    product.setQuantity(item.getQuantity());
                    return product;
                })
                .toList();

        cartDTO.setProducts(products);

        return cartDTO;
    }

    public List<ExternalServiceCartResponse> 
    (String productId) {

        List<CartItem> cartItems = cartItemRepository.findByProductId(productId);

        if (cartItems.isEmpty()) {
            return List.of();
        }

        Map<Long, List<CartItem>> cartGrouped =
                cartItems.stream().collect(Collectors.groupingBy(
                        item -> item.getCart().getCartId()
                ));

        return cartGrouped.entrySet().stream().map(entry -> {

            Long cartId = entry.getKey();
            List<CartItem> items = entry.getValue();

            ExternalServiceCartResponse response = new ExternalServiceCartResponse();
            response.setCartId(cartId);
            response.setUserId(items.get(0).getCart().getUserId());
            response.setTotalPrice(items.get(0).getCart().getTotalPrice());

            List<ExternalServiceCartItemResponse> itemResponses =
                    items.stream().map(item -> {

                        ExternalServiceCartItemResponse itemDTO =
                                new ExternalServiceCartItemResponse();

                        itemDTO.setCartItemId(item.getCartItemId());
                        itemDTO.setCartId(cartId);
                        itemDTO.setProductId(item.getProductId());
                        itemDTO.setQuantity(item.getQuantity());
                        itemDTO.setDiscount(item.getDiscount());
                        itemDTO.setProductPrice(item.getProductPrice());

                        return itemDTO;

                    }).toList();

            response.setExternalServiceCartItems(itemResponses);

            return response;

        }).toList();
    }

}
