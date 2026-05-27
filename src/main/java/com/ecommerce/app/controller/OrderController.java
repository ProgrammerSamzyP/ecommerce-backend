package com.ecommerce.app.controller;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.service.PdfService;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PdfService pdfService;

    // ---------- User endpoints ----------

    // Place an order (requires authentication)
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Order order, Authentication auth) {
        order.setUserEmail(auth.getName());
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);
        System.err.println("=== ORDER SAVED: ID=" + savedOrder.getId() + " Status=" + savedOrder.getStatus() + " ===");
        return ResponseEntity.ok(savedOrder);
    }

    // Get order history for the logged-in user (excludes customer-deleted orders)
    @GetMapping
    public List<Order> getOrders(Authentication auth) {
        return orderRepository.findByUserEmailAndCustomerDeletedFalseAndStatusNotIn(
                auth.getName(),
                List.of("CANCELLED")
        );
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> getInvoice(@PathVariable String id, Authentication auth) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();

        // Check access (owner or admin)
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
        if (!order.getUserEmail().equals(auth.getName()) && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        try {
            // If createdAt is null (old order), set a temporary value for the PDF
            if (order.getCreatedAt() == null) {
                order.setCreatedAt(java.time.LocalDateTime.now());
            }
            byte[] pdf = pdfService.generateInvoice(order);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=invoice-" + order.getId().substring(0,8) + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            System.err.println("Error generating invoice for order " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Cancel order (user can cancel their own pending orders)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String id, Authentication auth) {
        return orderRepository.findById(id)
                .map(order -> {
                    if (!order.getUserEmail().equals(auth.getName())) {
                        return ResponseEntity.status(403).body("Not your order");
                    }
                    if (!"PENDING".equals(order.getStatus()) && !"PENDING_PAYMENT".equals(order.getStatus())) {
                        return ResponseEntity.badRequest().body("Order cannot be cancelled");
                    }
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);
                    return ResponseEntity.ok(order);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Customer deletes an order from their history (soft‑delete for customer)
    @PutMapping("/{id}/customer-delete")
    public ResponseEntity<?> customerDeleteOrder(@PathVariable String id, Authentication auth) {
        return orderRepository.findById(id)
                .map(order -> {
                    if (!order.getUserEmail().equals(auth.getName())) {
                        return ResponseEntity.status(403).body("Not your order");
                    }
                    if (!"DELIVERED".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
                        return ResponseEntity.badRequest().body("Order must be delivered or cancelled before deleting");
                    }
                    order.setCustomerDeleted(true);
                    orderRepository.save(order);
                    return ResponseEntity.ok("Order deleted from your history");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- Admin endpoints ----------

    // Get all active (non‑admin‑deleted) orders
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() {
        return orderRepository.findByAdminDeletedFalseAndStatusNotIn(
                List.of("CANCELLED")
        );
    }

    // Get all admin‑deleted orders (trash)
    @GetMapping("/admin/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getDeletedOrders() {
        return orderRepository.findByAdminDeletedTrue();
    }

    // Update order status (only for active orders)
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String id,
                                                   @RequestBody Map<String, String> body) {
        return (ResponseEntity<Order>) orderRepository.findById(id)
                .map(order -> {
                    if (order.isAdminDeleted()) {
                        return ResponseEntity.badRequest().build(); // cannot update a trashed order
                    }
                    String newStatus = body.get("status");
                    if (newStatus != null) {
                        order.setStatus(newStatus.toUpperCase());
                        orderRepository.save(order);
                    }
                    return ResponseEntity.ok(order);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Soft‑delete for admin (move to trash)
    @PutMapping("/admin/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> softDeleteOrder(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setAdminDeleted(true);
                    orderRepository.save(order);
                    return ResponseEntity.ok("Order moved to trash");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Restore a trashed order
    @PutMapping("/admin/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> restoreOrder(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setAdminDeleted(false);
                    orderRepository.save(order);
                    return ResponseEntity.ok("Order restored");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Permanently delete an order
    @DeleteMapping("/admin/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> permanentlyDeleteOrder(@PathVariable String id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return ResponseEntity.ok("Order permanently deleted");
        }
        return ResponseEntity.notFound().build();
    }
}