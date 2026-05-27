package com.ecommerce.app.controller;

import com.ecommerce.app.dto.DailyRevenueDto;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    @Autowired
    private OrderRepository orderRepo;

    @GetMapping("/revenue-daily")
    public List<DailyRevenueDto> getDailyRevenue() {
        List<Order> allOrders = orderRepo.findAll();

        Map<LocalDate, Double> daily = allOrders.stream()
                .filter(o -> "PAID".equals(o.getStatus())
                        || "SHIPPING".equals(o.getStatus())
                        || "DELIVERED".equals(o.getStatus()))
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate(),
                        Collectors.summingDouble(Order::getTotal)
                ));

        return daily.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DailyRevenueDto(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        // Get all orders, then filter out cancelled ones for the stats
        List<Order> allOrders = orderRepo.findAll();
        List<Order> activeOrders = allOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .collect(Collectors.toList());

        double totalRevenue = activeOrders.stream()
                .filter(o -> "PAID".equals(o.getStatus())
                        || "SHIPPING".equals(o.getStatus())
                        || "DELIVERED".equals(o.getStatus()))
                .mapToDouble(Order::getTotal)
                .sum();

        long totalOrders = activeOrders.size();
        long paidOrders = activeOrders.stream().filter(o -> "PAID".equals(o.getStatus())).count();
        long shippingOrders = activeOrders.stream().filter(o -> "SHIPPING".equals(o.getStatus())).count();
        long deliveredOrders = activeOrders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count();
        long pendingOrders = activeOrders.stream()
                .filter(o -> "PENDING".equals(o.getStatus()) || "PENDING_PAYMENT".equals(o.getStatus()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("paidOrders", paidOrders);
        stats.put("shippingOrders", shippingOrders);
        stats.put("deliveredOrders", deliveredOrders);
        stats.put("pendingOrders", pendingOrders);
        return ResponseEntity.ok(stats);
    }
}