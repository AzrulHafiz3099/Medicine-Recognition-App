package com.workshop2.medrecog;

public class Vendor {
    private String id;
    private String name;
    private String address;
    private String contact;
    private String email;

    public Vendor(String id, String name, String address, String contact, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }
}

