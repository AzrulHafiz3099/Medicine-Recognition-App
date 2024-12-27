package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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

public class StoreSelect extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VendorAdapter vendorAdapter;
    private List<Vendor> vendorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_select);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize vendor list
        vendorList = new ArrayList<>();

        // Set up the adapter
        vendorAdapter = new VendorAdapter(this, vendorList);
        recyclerView.setAdapter(vendorAdapter);

        // Fetch vendor data from the API
        fetchVendors();
    }

    private void fetchVendors() {
        String url = getString(R.string.api_vendor); // Replace with your actual API URL for fetching vendors

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

                                vendorList.clear(); // Clear existing vendor data

                                for (int i = 0; i < vendors.length(); i++) {
                                    JSONObject vendor = vendors.getJSONObject(i);
                                    String id = vendor.getString("VendorID");
                                    String fullname = vendor.getString("Fullname");
                                    String address = vendor.getString("Address");
                                    String contactNumber = vendor.getString("ContactNumber");
                                    String email = vendor.getString("Email");

                                    // Add the vendor to the list
                                    vendorList.add(new Vendor(id, fullname, address, contactNumber, email));
                                }

                                // Notify the adapter that data has changed
                                vendorAdapter.notifyDataSetChanged();
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(StoreSelect.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("VendorsResponseError", "JSON Parsing error", e);
                            Toast.makeText(StoreSelect.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VendorsError", "Error occurred", error);
                        Toast.makeText(StoreSelect.this, "Error fetching vendor data", Toast.LENGTH_SHORT).show();
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
}
