package com.app.ecommerce.orderservice.controller;

import com.app.ecommerce.orderservice.config.AppConstants;
import com.app.ecommerce.orderservice.payload.*;
import com.app.ecommerce.orderservice.service.OrderService;
import com.app.ecommerce.orderservice.util.AuthUtil;
import com.app.ecommerce.orderservice.util.PaymentUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private PaymentUtil paymentUtil;

    @PostMapping("/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(
            @PathVariable String paymentMethod,
            @RequestBody OrderRequestDTO orderRequestDTO) {

        String emailId = authUtil.loggedInEmail();

        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/stripe-client-secret")
    public ResponseEntity<Map<String, String>> createStripeClientSecret(
            @RequestBody StripePaymentDto stripePaymentDto) {

        Map<String, String> response =
                paymentUtil.createStripeClientSecret(stripePaymentDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ ADMIN → Get all orders
    @GetMapping
    public ResponseEntity<OrderResponse> getAllOrders(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_ORDERS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {
        return ResponseEntity.ok(
                orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder)
        );
    }

    @GetMapping("/seller")
    public ResponseEntity<OrderResponse> getAllSellerOrders(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_ORDERS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {
        return ResponseEntity.ok(
                orderService.getAllSellerOrders(pageNumber, pageSize, sortBy, sortOrder)
        );
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateDto dto) {

        return ResponseEntity.ok(
                orderService.updateOrder(orderId, dto.getStatus())
        );
    }

    @PutMapping("/seller/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatusSeller(@PathVariable Long orderId,
                                                            @RequestBody OrderStatusUpdateDto orderStatusUpdateDto) {
        OrderDTO order = orderService.updateOrder(orderId, orderStatusUpdateDto.getStatus());
        return new ResponseEntity<OrderDTO>(order, HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getOrderCount() {
        return ResponseEntity.ok(orderService.getOrderCount());
    }

    @GetMapping("/revenue")
    public ResponseEntity<Double> getTotalRevenue() {
        return ResponseEntity.ok(orderService.getTotalRevenue());
    }
}
