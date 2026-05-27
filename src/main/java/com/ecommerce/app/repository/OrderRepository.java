package com.ecommerce.app.repository;

import com.ecommerce.app.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserEmail(String userEmail);

    Order findByPaymentReference(String paymentReference);

    List<Order> findByUserEmailAndStatusNotIn(String userEmail, List<String> statuses);

    List<Order> findByUserEmailAndCustomerDeletedFalse(String userEmail);

    List<Order> findByAdminDeletedFalse();   // active orders (admin view)
    List<Order> findByAdminDeletedTrue();    // trash (admin view)
    List<Order> findByUserEmailAndCustomerDeletedFalseAndStatusNotIn(String userEmail, List<String> statuses);
    List<Order> findByAdminDeletedFalseAndStatusNotIn(List<String> statuses);

}