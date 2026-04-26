package com.app.ecommerce.paymentservice.service;

import com.app.ecommerce.paymentservice.model.Payment;
import com.app.ecommerce.paymentservice.payload.PaymentDTO;
import com.app.ecommerce.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    @Autowired
    PaymentRepository paymentRepository;


    public void savePayment(Long orderId, PaymentDTO paymentDTO) {
        Payment payment = new Payment();

        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setPgPaymentId(paymentDTO.getPgPaymentId());
        payment.setPgStatus(paymentDTO.getPgStatus());
        payment.setPgResponseMessage(paymentDTO.getPgResponseMessage());
        payment.setPgName(paymentDTO.getPgName());
        payment.setOrderId(orderId);

        Payment saved = paymentRepository.save(payment);

    }
}
