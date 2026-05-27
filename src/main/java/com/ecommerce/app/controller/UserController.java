package com.ecommerce.app.controller;

import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder encoder;

    // ---------- Get current user's profile ----------
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- Update profile (name, address, phone) ----------
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User updated, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(updated.getName());
        user.setAddress(updated.getAddress());
        user.setPhone(updated.getPhone());

        return ResponseEntity.ok(userRepo.save(user));
    }

    // ---------- Change password ----------
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest req,
                                            Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.oldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }

        user.setPassword(encoder.encode(req.newPassword()));
        userRepo.save(user);
        return ResponseEntity.ok("Password updated successfully");
    }

    // DTO for password change
    record PasswordChangeRequest(String oldPassword, String newPassword) {}
}