package com.ecommerce.app.controller;

import com.ecommerce.app.model.Coupon;
import com.ecommerce.app.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCouponController {

    @Autowired
    private CouponRepository repo;

    @GetMapping
    public List<Coupon> getAll() { return repo.findAll(); }

    @PostMapping
    public Coupon create(@RequestBody Coupon coupon) {
        return repo.save(coupon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> update(@PathVariable String id, @RequestBody Coupon updated) {
        return repo.findById(id).map(c -> {
            c.setCode(updated.getCode());
            c.setDiscount(updated.getDiscount());
            c.setActive(updated.isActive());
            c.setExpiryDate(updated.getExpiryDate());
            c.setUsageLimit(updated.getUsageLimit());
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}