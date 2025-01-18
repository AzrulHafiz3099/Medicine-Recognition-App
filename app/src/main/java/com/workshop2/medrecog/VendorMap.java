package com.workshop2.medrecog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.workshop2.medrecog.adapter.LocationAdapter;
import com.workshop2.medrecog.adapter.LocationItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class VendorMap extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GoogleMap mMap;
    private RecyclerView recyclerView;
    private List<LocationItem> locationList = new ArrayList<>();
    private boolean isMapReady = false;
    private ImageView imageBack;
    private String selectedVendorId; // Variable to store the selected vendor ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Get Vendor ID from Intent
        if (getIntent() != null) {
            selectedVendorId = getIntent().getStringExtra("VendorID");
        }

        // Initialize the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.card_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        fetchVendors(); // Fetch vendor data dynamically

        imageBack = findViewById(R.id.img_back); // Find the back button

        // Handle back button behavior
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close the activity
            }
        });

        imageBack.setOnClickListener(v -> onBackPressed()); // Set OnClickListener for back button
    }

    private void fetchVendors() {
        String url = getString(R.string.api_vendor); // API endpoint
        String imageBaseUrl = getString(R.string.vendor_image_url); // Base URL for images

        Log.d("FetchVendors", "Fetching vendors from URL: " + url); // Debug log for URL

        // Prepare the Volley request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("VendorsResponse", "Raw Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONArray vendors = jsonResponse.getJSONArray("vendors");

                                locationList.clear(); // Clear existing data
                                for (int i = 0; i < vendors.length(); i++) {
                                    JSONObject vendor = vendors.getJSONObject(i);

                                    String name = vendor.getString("Fullname");
                                    String address = vendor.getString("Address");
                                    String contactNumber = vendor.getString("ContactNumber");
                                    String email = vendor.getString("Email");
                                    double latitude = vendor.getDouble("Latitude");
                                    double longitude = vendor.getDouble("Longitude");
                                    String profilePicture = vendor.getString("ProfilePicture");
                                    String vendorId = vendor.getString("VendorID"); // Get the vendor ID

                                    // Combine base URL with the profile picture path
                                    String imageUrl = imageBaseUrl + profilePicture;

                                    // Add the vendor to the location list
                                    locationList.add(new LocationItem(
                                            name,
                                            address,
                                            "Open", // Example status, replace as needed
                                            "9:00 AM - 9:00 PM", // Example timings, replace as needed
                                            new LatLng(latitude, longitude),
                                            imageUrl,
                                            vendorId // Include the vendor ID in the LocationItem
                                    ));
                                }

                                // Setup adapter and notify changes
                                LocationAdapter adapter = new LocationAdapter(locationList, new LocationAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(int position) {
                                        LatLng selectedPosition = locationList.get(position).getLatLng();
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPosition, 18));

                                        // Update the map markers based on the selected vendor
                                        String clickedVendorId = locationList.get(position).getVendorId();
                                        updateMarkersForVendor(clickedVendorId);
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
                                        Toast.makeText(VendorMap.this, "Store selected: " + locationList.get(position).getName(), Toast.LENGTH_SHORT).show();

                                        // Get the selected vendor ID
                                        String selectedVendorId = locationList.get(position).getVendorId();

                                        // Create an Intent to navigate to Homepage.class
                                        Intent intent = new Intent(VendorMap.this, Homepage.class);

                                        // Add the vendor ID as an extra to the Intent
                                        intent.putExtra("VendorID", selectedVendorId);

                                        // Start the Homepage activity
                                        startActivity(intent);
                                    }
                                });
                                recyclerView.setAdapter(adapter);

                                // Add markers for only the selected vendor if map is already ready
                                if (isMapReady && selectedVendorId != null && !selectedVendorId.isEmpty()) {
                                    updateMarkersForVendor(selectedVendorId); // Show only selected vendor markers
                                }

                                // Scroll to selected vendor if ID is provided
                                if (selectedVendorId != null && !selectedVendorId.isEmpty()) {
                                    for (int i = 0; i < locationList.size(); i++) {
                                        if (locationList.get(i).getVendorId().equals(selectedVendorId)) {
                                            recyclerView.scrollToPosition(i); // Scroll to the vendor position
                                            break;
                                        }
                                    }
                                }
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(VendorMap.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("VendorsResponseError", "JSON Parsing error", e);
                            Toast.makeText(VendorMap.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VendorsError", "Error occurred", error);
                        Toast.makeText(VendorMap.this, "Error fetching vendor data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getVendors");
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }


    // Function to update markers based on selected vendor
    private void updateMarkersForVendor(String vendorId) {
        mMap.clear(); // Clear existing markers

        for (LocationItem locationItem : locationList) {
            if (locationItem.getVendorId().equals(vendorId)) {
                mMap.addMarker(new MarkerOptions()
                        .position(locationItem.getLatLng())
                        .title(locationItem.getName()));
            }
        }

        // Optionally zoom into the first marker
        if (!locationList.isEmpty()) {
            LatLng initialLocation = locationList.get(0).getLatLng();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));
        }
    }


    private void addMarkersToMap() {
        for (LocationItem locationItem : locationList) {
            mMap.addMarker(new MarkerOptions()
                    .position(locationItem.getLatLng())
                    .title(locationItem.getName()));
        }

        if (!locationList.isEmpty()) {
            LatLng initialLocation = locationList.get(0).getLatLng();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true; // Mark the map as ready
        if (!locationList.isEmpty()) {
            addMarkersToMap(); // Add markers if data is already fetched
        }
    }
}



