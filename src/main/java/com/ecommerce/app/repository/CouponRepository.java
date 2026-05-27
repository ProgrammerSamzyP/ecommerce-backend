package com.ecommerce.app.repository;

import com.ecommerce.app.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CouponRepository extends MongoRepository<Coupon, String> {
    Optional<Coupon> findByCodeAndActiveTrue(String code);
}