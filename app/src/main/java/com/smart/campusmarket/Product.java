package com.smart.campusmarket;
import java.io.Serializable;

//Make Product.java Implement Serializable
//To pass an entire Product object through an Intent instead of passing every string field individually, make Product implement java.io.Serializable
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String price;
    private String location;
    private String timePosted;
    //create a constructor for the Product class
    public Product(String title, String price,String location, String timePosted){
        this.title =title;
        this.price =price;
        this.location=location;
        this.timePosted=timePosted;
    }

    // Getters so the Adapter can read the data
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getLocation() { return location; }
    public String getTimePosted() { return timePosted; }

}
