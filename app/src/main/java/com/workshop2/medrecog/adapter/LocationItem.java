package com.workshop2.medrecog.adapter;

import com.google.android.gms.maps.model.LatLng;

public class LocationItem {
    private String name;
    private String distance;
    private String status;
    private String openingTime;
    private LatLng latLng;
    private String imageUrl; // Updated to use a String for the image URL
    private String vendorId; // Add vendorId field

    public LocationItem(String name, String distance, String status, String openingTime, LatLng latLng, String imageUrl, String vendorId) {
        this.name = name;
        this.distance = distance;
        this.status = status;
        this.openingTime = openingTime;
        this.latLng = latLng;
        this.imageUrl = imageUrl;
        this.vendorId = vendorId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVendorId() {
        return vendorId;
    }
}


//public Location getLocation() {
//        Location location = new Location("");
//        location.setLatitude(latLng.latitude);
//        location.setLongitude(latLng.longitude);
//        return location;
//    }
//}


