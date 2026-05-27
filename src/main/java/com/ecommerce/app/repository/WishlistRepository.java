package com.ecommerce.app.repository;

import com.ecommerce.app.model.WishlistItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends MongoRepository<WishlistItem, String> {
    List<WishlistItem> findByUserEmail(String userEmail);
    Optional<WishlistItem> findByUserEmailAndProductId(String userEmail, String productId);
    void deleteByUserEmailAndProductId(String userEmail, String productId);
    boolean existsByUserEmailAndProductId(String userEmail, String productId);
}