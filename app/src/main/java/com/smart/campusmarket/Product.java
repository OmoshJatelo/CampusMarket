package com.smart.campusmarket;

import java.io.Serializable;

public class Product implements Serializable {
    private String title;
    private String price;
    private String location;
    private String timePosted;
    private String sellerPhone; // New field

    public Product(String title, String price, String location, String timePosted, String sellerPhone) {
        this.title = title;
        this.price = price;
        this.location = location;
        this.timePosted = timePosted;
        this.sellerPhone = sellerPhone;
    }

    // Existing getters...
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getLocation() { return location; }
    public String getTimePosted() { return timePosted; }

    // New getter
    public String getSellerPhone() { return sellerPhone; }
}