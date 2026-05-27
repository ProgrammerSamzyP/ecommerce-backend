package com.ecommerce.app.repository;


import com.ecommerce.app.model.User;  // your own User entity
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);   // must return Optional
    Optional<User> findByResetToken(String resetToken);
    Boolean existsByEmail(String email);
}
