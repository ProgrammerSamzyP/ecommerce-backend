package com.ecommerce.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shipping_rates")
public class ShippingRate {
    @Id
    private String id;
    private String location;   // e.g. "Lagos", "Abuja"
    private double cost;

    public ShippingRate() {}
    public ShippingRate(String location, double cost) {
        this.location = location;
        this.cost = cost;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}