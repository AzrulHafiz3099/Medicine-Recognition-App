package com.workshop2.medrecog.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class LocationItem {
    private String name;
    private String distance;
    private String status;
    private String openingTime;
    private LatLng latLng;
    private int imageDrawableId; // Add this line


    public LocationItem(String name, String distance, String status, String openingTime, LatLng latLng, int imageDrawableId) {
        this.name = name;
        this.distance = distance;
        this.status = status;
        this.openingTime = openingTime;
        this.latLng = latLng;
        this.imageDrawableId = imageDrawableId;
    }

    // Getter and Setter for distance
    public String getDistance() {
        return distance;
    }

    // Getter for imageUrl
    public int getImageDrawableId() {
        return imageDrawableId;
    }
    public Drawable getDrawable(Context context){
        return context.getResources().getDrawable(imageDrawableId);
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    // Other getters and setters
    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public Location getLocation() {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }
}


