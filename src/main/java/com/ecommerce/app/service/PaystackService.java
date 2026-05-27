package com.ecommerce.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaystackService {

    @Value("${paystack.secret.key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map initializeTransaction(String email, double amountNaira, String reference) {
        String url = "https://api.paystack.co/transaction/initialize";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        int amountKobo = (int)(amountNaira * 100);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("amount", amountKobo);
        body.put("reference", reference);          // ← our unique reference
        body.put("callback_url", "http://localhost:3000/order-confirmation");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return response.getBody();
    }

    public Map verifyTransaction(String reference) {
        try {
            String url = "https://api.paystack.co/transaction/verify/" + reference;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            Map body = response.getBody();
            System.err.println("PAYSTACK VERIFY RESPONSE: " + body);
            return body;
        } catch (Exception e) {
            System.err.println("Paystack verification error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", false);
            errorResponse.put("message", "Verification failed due to network or API error");
            Map<String, Object> data = new HashMap<>();
            data.put("status", "error");
            errorResponse.put("data", data);
            return errorResponse;
        }
    }
}