package com.workshop2.medrecog;

public class Drug {
    private String id;
    private String name;
    private String image;
    private double price; // Price is a double

    // Constructor
    public Drug(String id, String name, String image, double price) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) { // Accept double instead of String
        this.price = price;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
