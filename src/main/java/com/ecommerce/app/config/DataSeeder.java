package com.ecommerce.app.config;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository userRepo,
                           ProductRepository productRepo,
                           PasswordEncoder encoder) {
        return args -> {

            // 1️⃣ Seed admin user (if it doesn't exist)
            if (!userRepo.existsByEmail("admin@ecommerce.com")) {
                User admin = new User();
                admin.setEmail("admin@ecommerce.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setName("Admin");
                admin.setRole("ADMIN");                     // admin role
                userRepo.save(admin);
                System.out.println("✅ Admin user created (admin@ecommerce.com / admin123)");
            } else {
                System.out.println("ℹ️ Admin user already exists.");
            }

            // 2️⃣ Seed demo products (only if collection is empty)
            if (productRepo.count() == 0) {
                productRepo.save(new Product(null, "Wireless Mouse", 29.99, "https://example.com/mouse.jpg"));
                productRepo.save(new Product(null, "Mechanical Keyboard", 89.99, "https://example.com/keyboard.jpg"));
                productRepo.save(new Product(null, "USB-C Hub", 49.99, "https://example.com/hub.jpg"));
                System.out.println("✅ Demo products added.");
            } else {
                System.out.println("ℹ️ Products already exist – skipping seed.");
            }
        };
    }
}