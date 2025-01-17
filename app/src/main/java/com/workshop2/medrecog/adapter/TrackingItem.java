package com.workshop2.medrecog.adapter;

import java.util.List;

public class TrackingItem {
    public String orderNumber;
    public String placedDate;
    public int itemCount; // Add item count
    public String itemTotal; // Add item total
    public List<TrackingStep> trackingSteps;

    // Constructor (add itemCount and itemTotal)
    public TrackingItem(String orderNumber, String placedDate, int itemCount, String itemTotal, List<TrackingStep> trackingSteps) {
        this.orderNumber = orderNumber;
        this.placedDate = placedDate;
        this.itemCount = itemCount;
        this.itemTotal = itemTotal;
        this.trackingSteps = trackingSteps;
    }
}

