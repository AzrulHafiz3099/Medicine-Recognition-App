package com.workshop2.medrecog;

public class Cart {
    private String cartItemID;
    private String cartID;
    private String drugID;
    private String genericName;
    private String drugImage;
    private int quantity;
    private double price;

    public Cart(String cartItemID, String cartID, String drugID, String genericName, String drugImage, int quantity, double price) {
        this.cartItemID = cartItemID;
        this.cartID = cartID;
        this.drugID = drugID;
        this.genericName = genericName;
        this.drugImage = drugImage;
        this.quantity = quantity;
        this.price = price;
    }

    public String getCartItemID() {
        return cartItemID;
    }

    public String getCartID() {
        return cartID;
    }

    public String getDrugID() {
        return drugID;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getDrugImage() {
        return drugImage;
    }

    public int getQuantity() {
        return quantity;
    }

    // Add the setQuantity method
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }
}
