package com.ecommerce.app.repository;

import com.ecommerce.app.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByDeletedFalse();    // active products
    List<Product> findByDeletedTrue();     // deleted products (trash)
}
