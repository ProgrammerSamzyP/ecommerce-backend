package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;

    // Create a new product
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        // ensure deleted is false by default
        product.setDeleted(false);
        return productRepository.save(product);
    }

    // Get all active products (for admin list)
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalse();
    }

    // Get all deleted products (trash)
    @GetMapping("/deleted")
    public List<Product> getDeletedProducts() {
        return productRepository.findByDeletedTrue();
    }

    // Update a product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product updatedProduct) {
        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        Product product = existing.get();
        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setImageUrl(updatedProduct.getImageUrl());
        product.setStock(updatedProduct.getStock());
        product.setAdditionalImages(updatedProduct.getAdditionalImages());   // ← ADD THIS LINE

        return ResponseEntity.ok(productRepository.save(product));
    }

    // Soft delete (move to trash)
    @PutMapping("/{id}/delete")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable String id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setDeleted(true);
            productRepository.save(p);
        });
        return ResponseEntity.noContent().build();
    }

    // Restore a deleted product
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreProduct(@PathVariable String id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setDeleted(false);
            productRepository.save(p);
        });
        return ResponseEntity.noContent().build();
    }

    // Permanently delete a product
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDelete(@PathVariable String id) {
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}