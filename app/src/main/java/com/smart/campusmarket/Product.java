package com.smart.campusmarket;

public class Product {
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
