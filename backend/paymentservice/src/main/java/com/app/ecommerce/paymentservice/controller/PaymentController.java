package com.app.ecommerce.paymentservice.controller;

import com.app.ecommerce.paymentservice.payload.PaymentDTO;
import com.app.ecommerce.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.ecommerce.paymentservice.payload.StripePaymentDto;
import com.app.ecommerce.paymentservice.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/payments")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private PaymentService paymentService;

    // Create Stripe Payment Intent
    @PostMapping("/create")
    public ResponseEntity<?> createPaymentIntent(
            @RequestBody StripePaymentDto stripePaymentDto
    ) throws StripeException {

        PaymentIntent paymentIntent =
                stripeService.paymentIntent(stripePaymentDto);

        return new ResponseEntity<>(paymentIntent, HttpStatus.CREATED);
    }

    // Get Payment by ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentIntent(@PathVariable String paymentId)
            throws StripeException {

        PaymentIntent paymentIntent =
                PaymentIntent.retrieve(paymentId);

        return ResponseEntity.ok(paymentIntent);
    }

    // Save Payment after Order
    @PostMapping("/order/{orderId}")
    public ResponseEntity<Void> savePayment(
            @PathVariable Long orderId,
            @RequestBody PaymentDTO paymentDTO) {

        paymentService.savePayment(orderId, paymentDTO);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
