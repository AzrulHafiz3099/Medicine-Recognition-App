package com.workshop2.medrecog.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public class LocationProvider {

    private final LocationManager locationManager;
    private final LocationListener locationListener;

    public LocationProvider(Context context, LocationListener locationListener) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.locationListener = locationListener;
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }
}

