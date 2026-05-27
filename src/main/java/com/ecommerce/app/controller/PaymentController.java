package com.ecommerce.app.controller;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.service.EmailService;
import com.ecommerce.app.service.PaystackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/paystack")
public class PaymentController {

    @Autowired
    private PaystackService paystackService;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository productRepo;   // ✅ correct repository

    @Autowired
    private EmailService emailService;

    @PostMapping("/initialize/{orderId}")
    public ResponseEntity<?> initializePayment(@PathVariable String orderId, Authentication auth) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        if (!order.getUserEmail().equals(auth.getName())) {
            return ResponseEntity.status(403).body("Not your order");
        }

        // Use the reference that the frontend already stored in the order
        String reference = order.getPaymentReference();
        if (reference == null || reference.isEmpty()) {
            // fallback only if missing (should never happen with your frontend)
            reference = java.util.UUID.randomUUID().toString();
            order.setPaymentReference(reference);
        }

        order.setStatus("PENDING_PAYMENT");
        orderRepo.save(order);

        Map response = paystackService.initializeTransaction(
                order.getUserEmail(),
                order.getTotal(),
                reference
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<?> verifyPayment(@PathVariable String reference) {
        // 1. Verify with Paystack
        Map response = paystackService.verifyTransaction(reference);
        Map data = (Map) response.get("data");
        if (data == null) {
            return ResponseEntity.ok(Map.of(
                    "status", "VERIFICATION_FAILED",
                    "message", "No data from Paystack"
            ));
        }

        String paystackStatus = (String) data.get("status");
        if (!"success".equals(paystackStatus)) {
            return ResponseEntity.ok(Map.of(
                    "status", "VERIFICATION_FAILED",
                    "message", "Payment not successful"
            ));
        }

        // 2. Find the order
        Order order = orderRepo.findByPaymentReference(reference);
        if (order == null) {
            return ResponseEntity.ok(Map.of(
                    "status", "ORDER_NOT_FOUND",
                    "message", "No order for this reference"
            ));
        }

        // 3. Decrement stock for each item in the order (only after successful payment)
        for (Order.OrderItem item : order.getItems()) {
            Product product = productRepo.findById(item.getProductId()).orElse(null);
            if (product != null) {
                int newStock = product.getStock() - item.getQuantity();
                if (newStock < 0) newStock = 0;   // avoid negative
                product.setStock(newStock);
                productRepo.save(product);
            }
        }

        // 4. Mark order as PAID and save
        order.setStatus("PAID");
        orderRepo.save(order);
        try {
            emailService.sendOrderConfirmation(order.getUserEmail(), order);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("status", order.getStatus()));
    }
}