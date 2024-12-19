package com.workshop2.medrecog.adapter;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class    LocationItem {
    private String name;
    private String distance;
    private String status;
    private String openingTime;
    private LatLng latLng;

    public LocationItem(String name, String distance, String status, String openingTime, LatLng latLng) {
        this.name = name;
        this.distance = distance;
        this.status = status;
        this.openingTime = openingTime;
        this.latLng = latLng;
    }

    // Getter and Setter for distance
    public String getDistance() {
        return distance;
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


