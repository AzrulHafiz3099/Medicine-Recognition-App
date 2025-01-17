package com.workshop2.medrecog.adapter;

import java.util.List;

public class TrackingItem {
    public String orderNumber;
    public String placedDate;
    public int itemCount; // Add item count
    public String itemTotal; // Add item total
    public List<TrackingStep> trackingSteps;
    String paymentMethod;
    String paymentStatus;

    // Constructor (add itemCount and itemTotal)
    public TrackingItem(String orderNumber, String placedDate, int itemCount, String itemTotal, List<TrackingStep> trackingSteps, String paymentMethod, String paymentStatus) {
        this.orderNumber = orderNumber;
        this.placedDate = placedDate;
        this.itemCount = itemCount;
        this.itemTotal = itemTotal;
        this.trackingSteps = trackingSteps;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}

