package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.WishlistItem;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepo;

    @Autowired
    private ProductRepository productRepo;

    // Get all wishlist products for current user
    @GetMapping
    public List<Product> getWishlist(Authentication auth) {
        return wishlistRepo.findByUserEmail(auth.getName())
                .stream()
                .map(w -> productRepo.findById(w.getProductId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Add product to wishlist
    @PostMapping("/{productId}")
    public ResponseEntity<?> addToWishlist(@PathVariable String productId, Authentication auth) {
        if (!wishlistRepo.existsByUserEmailAndProductId(auth.getName(), productId)) {
            wishlistRepo.save(new WishlistItem(null, auth.getName(), productId));
        }
        return ResponseEntity.ok().build();
    }

    // Remove product from wishlist
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable String productId, Authentication auth) {
        wishlistRepo.deleteByUserEmailAndProductId(auth.getName(), productId);
        return ResponseEntity.ok().build();
    }

    // Check if product is in wishlist (useful for heart icon)
    @GetMapping("/check/{productId}")
    public boolean isInWishlist(@PathVariable String productId, Authentication auth) {
        return wishlistRepo.existsByUserEmailAndProductId(auth.getName(), productId);
    }
}