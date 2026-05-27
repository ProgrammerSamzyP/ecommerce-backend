package com.ecommerce.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wishlist")
@CompoundIndex(def = "{'userEmail':1, 'productId':1}", unique = true)
public class WishlistItem {
    @Id
    private String id;
    private String userEmail;
    private String productId;

    public WishlistItem() {}
    public WishlistItem(String id, String userEmail, String productId) {
        this.id = id;
        this.userEmail = userEmail;
        this.productId = productId;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}