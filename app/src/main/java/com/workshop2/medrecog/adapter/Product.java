package com.workshop2.medrecog.adapter;

public class Product {
    private String BrandName;
    private String GenericName;

    public Product(String brandName, String genericName) {
        BrandName = brandName;
        GenericName = genericName;
    }

    public String getBrandName() {
        return BrandName;
    }

    public String getGenericName() {
        return GenericName;
    }
}
