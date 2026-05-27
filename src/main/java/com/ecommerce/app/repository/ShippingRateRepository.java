package com.ecommerce.app.repository;

import com.ecommerce.app.model.ShippingRate;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ShippingRateRepository extends MongoRepository<ShippingRate, String> {
    Optional<ShippingRate> findByLocation(String location);
}