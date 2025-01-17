package com.workshop2.medrecog.adapter;

public class TrackingStep {
    public String status;
    public String date;
    public boolean isCompleted;

    // Constructor
    public TrackingStep(String status, String date, boolean isCompleted) {
        this.status = status;
        this.date = date;
        this.isCompleted = isCompleted;
    }
}
