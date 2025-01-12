package com.workshop2.medrecog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.workshop2.medrecog.adapter.LocationAdapter;
import com.workshop2.medrecog.adapter.LocationItem;
import com.workshop2.medrecog.adapter.LocationProvider;

import java.util.ArrayList;
import java.util.List;

public class Map extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GoogleMap mMap;
    private RecyclerView recyclerView;
    private List<LocationItem> locationList;

    private Location myCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Initialize the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.card_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Populate the RecyclerView with data
        locationList = new ArrayList<>();
        locationList.add(new LocationItem(
                "Farmasi Durian Tunggal",
                "",
                "Open",
                "9:00 AM - 9:00 PM",
                new LatLng(2.3127839011974167, 102.28256742721523),
                R.drawable.farmasi_duriantunggal

        ));
        locationList.add(new LocationItem(
                "Farmasi NK Melaka",
                "",
                "Closed",
                "9:00 AM - 10:00 PM",
                new LatLng(2.3113589516536104, 102.27986996771712),
                R.drawable.farmasi_nk
        ));

        LocationAdapter adapter = new LocationAdapter(locationList, new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                LatLng selectedPosition = locationList.get(position).getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPosition, 18));
            }

            @Override
            public void onGetDirectionsClick(int position) {
                LatLng selectedPosition = locationList.get(position).getLatLng();
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + selectedPosition.latitude + "," + selectedPosition.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }

            @Override
            public void onSelectStoreClick(int position) {
                Toast.makeText(Map.this, "Store selected: " + locationList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        // Add scroll listener to auto-zoom on marker
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visiblePosition = layoutManager.findFirstVisibleItemPosition();
                        if (visiblePosition != RecyclerView.NO_POSITION) {
                            LatLng position = locationList.get(visiblePosition).getLatLng();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
                        }
                    }
                }
            }
        });

        requestLocationPermission();
    }


    private void onGetDirectionsClick(int position) {
        if (myCurrentLocation != null) {
            LatLng destination = locationList.get(position).getLatLng();

            // Construct the Google Maps intent
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination.latitude + "," + destination.longitude + "&mode=d"); // "d" for driving
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Handle the case where Google Maps is not installed
                Toast.makeText(this, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
                // Optionally, redirect to the Play Store
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps")));
                }
            }
        } else {
            Toast.makeText(Map.this, "Waiting for current location...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        for (LocationItem locationItem : locationList) {
            mMap.addMarker(new MarkerOptions()
                    .position(locationItem.getLatLng())
                    .title(locationItem.getName()));
        }

        // Move the camera to the first location
        LatLng initialLocation = locationList.get(0).getLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchUserLocation();
        }
    }

    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationProvider locationProvider = new LocationProvider(this, location -> {
                if (location != null) {
                    updateDistances(location);
                }
            });
            locationProvider.requestLocationUpdates();
        }
    }

    private void updateDistances(Location userLocation) {
        for (LocationItem locationItem : locationList) {
            LatLng destination = locationItem.getLatLng();
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), destination.latitude, destination.longitude, results);
            float distanceInMeters = results[0];
            String distanceText = String.format("%.1f km", distanceInMeters / 1000);
            locationItem.setDistance(distanceText);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                Toast.makeText(this, "Location permission is required to calculate distances.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
