package com.ecommerce.app.controller;

import com.ecommerce.app.model.ShippingRate;
import com.ecommerce.app.repository.ShippingRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    @Autowired
    private ShippingRateRepository repo;

    @GetMapping("/cost")
    public ResponseEntity<Double> getCost(@RequestParam String location) {
        return repo.findByLocation(location)
                .map(rate -> ResponseEntity.ok(rate.getCost()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/locations")
    public List<String> getLocations() {
        return repo.findAll().stream()
                .map(ShippingRate::getLocation)
                .collect(Collectors.toList());
    }
}