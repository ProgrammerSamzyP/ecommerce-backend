package com.ecommerce.app.controller;

import com.ecommerce.app.model.ShippingRate;
import com.ecommerce.app.repository.ShippingRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipping")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShippingController {

    @Autowired
    private ShippingRateRepository repo;

    @GetMapping
    public List<ShippingRate> getAll() { return repo.findAll(); }

    @PostMapping
    public ShippingRate create(@RequestBody ShippingRate rate) {
        return repo.save(rate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingRate> update(@PathVariable String id, @RequestBody ShippingRate updated) {
        return repo.findById(id).map(r -> {
            r.setLocation(updated.getLocation());
            r.setCost(updated.getCost());
            return ResponseEntity.ok(repo.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}