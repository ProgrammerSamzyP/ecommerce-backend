package com.ecommerce.app.service;

import com.ecommerce.app.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to MyShop!");
        message.setText("Hi " + name + ",\n\nThank you for registering at MyShop!\n\nHappy shopping!");
        mailSender.send(message);
    }

    public void sendOrderConfirmation(String toEmail, Order order) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Order Confirmation #" + order.getId().substring(0,8));
        msg.setText("Thank you for your order!\n\n" +
                "Order ID: " + order.getId() + "\n" +
                "Total: ₦" + order.getTotal() + "\n" +
                "Status: PAID\n\n" +
                "We'll notify you when your order ships.");
        mailSender.send(msg);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Password Reset Request");
        msg.setText("Click the link to reset your password: " + resetLink + "\n\nThis link expires in 1 hour.");
        mailSender.send(msg);
    }

    // you can add order confirmation, password reset, etc.
}