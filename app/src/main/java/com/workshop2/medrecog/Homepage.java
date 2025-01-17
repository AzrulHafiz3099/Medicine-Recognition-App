package com.workshop2.medrecog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Homepage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private ImageView imageIcon; // Search icon
    private TextView profileName;
    private Spinner spinner;
    private ArrayList<String> vendorNames;
    private ArrayList<String> vendorIds;
    private RecyclerView recyclerView;
    private DrugAdapter adapter;
    private List<Drug> drugList; // This will hold the drug data
    private String vendorIdFromIntent; // To store VendorID passed from the previous activity
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // Initialize DrawerLayout and menu icon
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.image_group); // Menu icon
        profileName = findViewById(R.id.container_group1);
        spinner = findViewById(R.id.spinner_vendor);
        recyclerView = findViewById(R.id.recycler_view);
        imageIcon = findViewById(R.id.image_icon);

        vendorNames = new ArrayList<>();
        vendorIds = new ArrayList<>();
        drugList = new ArrayList<>();

        // Set up RecyclerView with GridLayoutManager
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        adapter = new DrugAdapter(this, drugList, null);
        recyclerView.setAdapter(adapter);

        // Menu icon click listener to open sidebar
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Back button functionality for closing the sidebar
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        });

        // Fetch vendor and drug data
        fetchVendors();
        fetchDrugs();

        // Search icon click listener
        imageIcon.setOnClickListener(v -> {
            // Handle search icon click (if needed)
        });



        // Spinner selection listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchDrugs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        getUserProfile();
        checkOrCreateCart();

        // 1. Find the LinearLayout by its ID
        LinearLayout searchContainer = findViewById(R.id.container_link8);
        LinearLayout mapContainer = findViewById(R.id.container_link6);

        // 2. Set the click listener using a lambda expression
        searchContainer.setOnClickListener(v -> {
            // This code will execute when the LinearLayout is clicked
            Intent intent = new Intent(Homepage.this, Search.class);
            startActivity(intent);
        });

        mapContainer.setOnClickListener(v -> {
            // This code will execute when the LinearLayout is clicked
            Intent intent = new Intent(Homepage.this, com.workshop2.medrecog.Map.class);
            startActivity(intent);
        });


    }

    @Override
    public void onBackPressed() {
        // Close the sidebar if it is open, otherwise, handle the default back press
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void getUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(R.string.api_url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if ("success".equals(status)) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            userID = data.getString("UserID");
                            String fullName = data.getString("Fullname");
                            String greetingMessage = "Hello " + fullName + ", what type of medicine do you need today?";
                            profileName.setText(greetingMessage);
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching profile", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getProfile");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void fetchVendors() {
        String url = getString(R.string.api_vendor);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            JSONArray vendors = jsonResponse.getJSONArray("vendors");

                            vendorNames.clear();
                            vendorIds.clear();

                            for (int i = 0; i < vendors.length(); i++) {
                                JSONObject vendor = vendors.getJSONObject(i);
                                String name = vendor.getString("Fullname");
                                String id = vendor.getString("VendorID");

                                vendorNames.add(name);
                                vendorIds.add(id);
                            }

                            // Set up adapter for the spinner
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Homepage.this,
                                    android.R.layout.simple_spinner_item, vendorNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing vendor data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching vendors", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getVendors");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                String token = sharedPreferences.getString("jwt_token", "");
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void fetchDrugs() {
        int selectedVendorPosition = spinner.getSelectedItemPosition();

        if (selectedVendorPosition == -1 || vendorIds.isEmpty()) {
            return;
        }

        String vendorId = vendorIds.get(selectedVendorPosition);
        String url = getString(R.string.api_drug_supply);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            JSONArray drugs = jsonResponse.getJSONArray("drugs");

                            drugList.clear();

                            for (int i = 0; i < drugs.length(); i++) {
                                JSONObject drug = drugs.getJSONObject(i);
                                String id = drug.getString("SupplyID");
                                String name = drug.getString("BrandName");
                                String image = getString(R.string.drug_image_url) + drug.getString("DrugImage");
                                double price = drug.getDouble("Price");

                                drugList.add(new Drug(id, name, image, price));
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing drug data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching drugs", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getDrugs");
                params.put("vendorID", vendorId);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                String token = sharedPreferences.getString("jwt_token", "");
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void onCartButtonClick(View view) {
        Intent intent = new Intent(Homepage.this, CartProduct.class);
        startActivity(intent);
    }

    private void checkOrCreateCart() {
        String url = getString(R.string.api_cart);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String savedUserID = sharedPreferences.getString("UserID", "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (!"success".equals(status)) {
                            Toast.makeText(this, "Error creating cart", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing cart data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error checking/creating cart", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "check_or_create_cart");
                params.put("UserID", savedUserID);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

//    public void searchMenu(View view) {
//        Intent intent = new Intent(Homepage.this, Search.class);
//        intent.putExtra("message", "Hello from CurrentActivity!");
//        startActivity(intent);
//    }
}