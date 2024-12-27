package com.workshop2.medrecog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Homepage extends AppCompatActivity {

    private ImageView imageIcon;
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

        profileName = findViewById(R.id.container_group1);

        // Initialize views and lists
        spinner = findViewById(R.id.spinner_vendor);
        recyclerView = findViewById(R.id.recycler_view);
        vendorNames = new ArrayList<>();
        vendorIds = new ArrayList<>();
        drugList = new ArrayList<>();

        // Get VendorID passed from the intent
        Intent intent = getIntent();
        vendorIdFromIntent = intent.getStringExtra("VendorID");

        // Set up RecyclerView with GridLayoutManager
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        adapter = new DrugAdapter(this, drugList, null);
        recyclerView.setAdapter(adapter);

        // Fetch vendor data
        fetchVendors();

        // Fetch drug data (initially with sample data)
        fetchDrugs();

        // Initialize the ImageView
        imageIcon = findViewById(R.id.image_icon);

        // Set the click listener on the image
        imageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to StoreSelect activity
                Intent intent = new Intent(Homepage.this, StoreSelect.class);
                startActivity(intent);
            }
        });

        // Set up spinner's OnItemSelectedListener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Call the fetchDrugs method when an item is selected
                fetchDrugs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: handle case when nothing is selected
            }
        });

        getUserProfile();
    }

    private void getUserProfile() {

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("JWT_TOKEN", "Token Sent in home page: " + token);


        String url = getString(R.string.api_url); // Replace with your actual API URL

        // Prepare the POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ProfileResponse", "Raw Response: " + response); // Log the raw response

                        if (response.startsWith("<")) { // Check if the response is an HTML error page
                            Toast.makeText(Homepage.this, "Unexpected server response (HTML)", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if ("success".equals(status)) {
                                JSONObject data = jsonResponse.getJSONObject("data");
                                userID = data.getString("UserID");
                                String fullName = data.getString("Fullname");
                                String greetingMessage = "Hello " + fullName + ", what type of medicine do you need today?";
                                profileName.setText(greetingMessage);

                                // Save the userID to SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("UserID", userID);
                                editor.apply(); // Save changes asynchronously

                                Log.d("UserID", "Fetched UserID: " + userID); // Log userID
                                // Update the adapter with the new userID
                                adapter = new DrugAdapter(Homepage.this, drugList, userID);
                                recyclerView.setAdapter(adapter); // Reassign the adapter to the RecyclerView
                                adapter.notifyDataSetChanged(); // Notify the adapter of data changes
                            } else {
                                String message = jsonResponse.getString("message");
                                Log.e("ProfileResponseError", "Error message: " + message);
                                Toast.makeText(Homepage.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("ProfileResponseError", "JSON Parsing error", e);
                            Toast.makeText(Homepage.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ProfileError", "Error occurred", error);
                        Toast.makeText(Homepage.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send user action and data to the PHP script
                Map<String, String> params = new HashMap<>();
                params.put("action", "getProfile");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Add JWT token in header
                return headers;
            }
        };
        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void fetchVendors() {
        String url = getString(R.string.api_vendor); // Replace with your API URL for fetching vendors

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

                                // If VendorID is passed from the previous page, select the corresponding vendor
                                if (vendorIdFromIntent != null) {
                                    int position = vendorIds.indexOf(vendorIdFromIntent);
                                    if (position != -1) {
                                        spinner.setSelection(position); // Set the selected vendor in the spinner
                                    }
                                }
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(Homepage.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("VendorsResponseError", "JSON Parsing error", e);
                            Toast.makeText(Homepage.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VendorsError", "Error occurred", error);
                        Toast.makeText(Homepage.this, "Error fetching vendor data", Toast.LENGTH_SHORT).show();
                    }
                }) {
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
        // Check if the spinner has a valid selection before proceeding
        int selectedVendorPosition = spinner.getSelectedItemPosition();

        if (selectedVendorPosition == -1 || vendorIds.isEmpty()) {
            // Show a message or handle the error if the selection is invalid
//            Toast.makeText(Homepage.this, "Please select a valid vendor", Toast.LENGTH_SHORT).show();
            return;  // Exit the method if no valid vendor is selected
        }

        String vendorId = vendorIds.get(selectedVendorPosition);  // Get selected vendor ID from spinner
        String url = getString(R.string.api_drug_supply); // API URL

        // Prepare the Volley request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("DrugsResponse", "Raw Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONArray drugs = jsonResponse.getJSONArray("drugs");

                                drugList.clear(); // Clear existing drug data

                                for (int i = 0; i < drugs.length(); i++) {
                                    JSONObject drug = drugs.getJSONObject(i);
                                    String id = drug.getString("SupplyID");
                                    String name = drug.getString("BrandName");
                                    String image = getString(R.string.drug_image_url) + drug.getString("DrugImage");
                                    double price = drug.getDouble("Price");

                                    // Add the drug to the list
                                    drugList.add(new Drug(id, name, image, price));
                                }

                                // Notify the adapter that data has changed
                                adapter.notifyDataSetChanged();
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(Homepage.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("DrugsResponseError", "JSON Parsing error", e);
                            Toast.makeText(Homepage.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("DrugsError", "Error occurred", error);
                        Toast.makeText(Homepage.this, "Error fetching drug data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getDrugs");
                params.put("vendorID", vendorId); // Send the selected vendor ID
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

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void onCartButtonClick(View view) {
        Intent intent = new Intent(Homepage.this, CartProduct.class);
        startActivity(intent);
    }
}
