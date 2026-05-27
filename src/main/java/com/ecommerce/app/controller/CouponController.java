package com.ecommerce.app.controller;

import com.ecommerce.app.model.Coupon;
import com.ecommerce.app.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponRepository repo;

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        Coupon coupon = repo.findByCodeAndActiveTrue(code).orElse(null);
        if (coupon == null)
            return ResponseEntity.badRequest().body("Invalid coupon code");

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now()))
            return ResponseEntity.badRequest().body("Coupon has expired");

        if (coupon.getUsageLimit() != -1 && coupon.getUsageCount() >= coupon.getUsageLimit())
            return ResponseEntity.badRequest().body("Coupon usage limit reached");

        return ResponseEntity.ok(Map.of(
                "discount", coupon.getDiscount(),
                "code", coupon.getCode()
        ));
    }
}