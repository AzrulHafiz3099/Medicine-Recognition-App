package com.workshop2.medrecog;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductDesc extends AppCompatActivity {

    private String supplyID;
    private LinearLayout containerDescriptionDetails, containerDosageDetails, containerManufacturerDetails, containerSideEffectsDetails;
    private TextView textTabDescription, textTabDosage, textTabManufacturer, textTabSideEffects;

    // Declare TextViews and ImageView for binding the data
    private TextView txtQuantity, txtGenericName, txtPrice;
    private ImageView DrugImageView, imageIcon;
    private TextView text_description_BrandName, text_description_GenericName, text_description_ActiveIngredient;
    private TextView text_dosage_Dosage, text_dosage_DosageForm;
    private TextView text_manufacturer_Manufacturer, text_manufacturer_ManufactureDate;
    private TextView text_side_effects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_desc);

        // Initialize the expandable sections
        containerDescriptionDetails = findViewById(R.id.container_description_details);
        containerDosageDetails = findViewById(R.id.container_dosage_details);
        containerManufacturerDetails = findViewById(R.id.container_manufacturer_details);
        containerSideEffectsDetails = findViewById(R.id.container_side_effects_details);

        // Initialize the tab TextViews
        textTabDescription = findViewById(R.id.text_tab_description);
        textTabDosage = findViewById(R.id.text_tab_dosage);
        textTabManufacturer = findViewById(R.id.text_tab_manufacturer);
        textTabSideEffects = findViewById(R.id.text_tab_side_effects);

        // Initialize the views where we want to display data
        txtQuantity = findViewById(R.id.txtQuantity);
        txtGenericName = findViewById(R.id.txtGenericName);
        txtPrice = findViewById(R.id.txtPrice);
        DrugImageView = findViewById(R.id.DrugImageView);
        imageIcon = findViewById(R.id.image_icon); // Find the imageIcon

        text_description_BrandName = findViewById(R.id.text_description_BrandName);
        text_description_GenericName = findViewById(R.id.text_description_GenericName);
        text_description_ActiveIngredient = findViewById(R.id.text_description_ActiveIngredient);

        text_dosage_Dosage = findViewById(R.id.text_dosage_Dosage);
        text_dosage_DosageForm = findViewById(R.id.text_dosage_DosageForm);

        text_manufacturer_Manufacturer = findViewById(R.id.text_manufacturer_Manufacturer);
        text_manufacturer_ManufactureDate = findViewById(R.id.text_manufacturer_ManufactureDate);

        text_side_effects = findViewById(R.id.text_side_effects);



        // Get the SupplyID passed from Homepage
        supplyID = getIntent().getStringExtra("SupplyID");

        // Log the received SupplyID
        if (supplyID != null) {
            Log.d("ProductDesc", "Received SupplyID: " + supplyID);
        } else {
            Log.d("ProductDesc", "No SupplyID received");
        }

        // By default, show Description details and highlight the Description tab
        showDescriptionDetails();
        highlightTab(textTabDescription);  // Highlight the description tab initially

        if (supplyID != null) {
            fetchDrugDetails();
        } else {
            Toast.makeText(this, "Error: No SupplyID passed", Toast.LENGTH_SHORT).show();
        }

        // Set click listener on the image_icon
        imageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to Homepage activity
                Intent intent = new Intent(ProductDesc.this, Homepage.class);
                startActivity(intent);
                finish(); // Optional: To close the ProductDesc activity if needed
            }
        });
    }

    // Toggle visibility for Description section
    public void toggleDescription(View view) {
        hideAllDetails();
        showDescriptionDetails();
        highlightTab(textTabDescription);
        textTabDescription.setPaintFlags(textTabDescription.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    // Toggle visibility for Dosage section
    public void toggleDosage(View view) {
        hideAllDetails();
        showDosageDetails();
        highlightTab(textTabDosage);
    }

    // Toggle visibility for Manufacturer section
    public void toggleManufacturer(View view) {
        hideAllDetails();
        showManufacturerDetails();
        highlightTab(textTabManufacturer);
    }

    // Toggle visibility for Side Effects section
    public void toggleSideEffects(View view) {
        hideAllDetails();
        showSideEffectsDetails();
        highlightTab(textTabSideEffects);
    }

    // Hide all detail sections
    private void hideAllDetails() {
        containerDescriptionDetails.setVisibility(View.GONE);
        containerDosageDetails.setVisibility(View.GONE);
        containerManufacturerDetails.setVisibility(View.GONE);
        containerSideEffectsDetails.setVisibility(View.GONE);

        // Reset all tabs to default color
        textTabDescription.setTextColor(getResources().getColor(R.color.default_tab_color));
        textTabDosage.setTextColor(getResources().getColor(R.color.default_tab_color));
        textTabManufacturer.setTextColor(getResources().getColor(R.color.default_tab_color));
        textTabSideEffects.setTextColor(getResources().getColor(R.color.default_tab_color));
    }

    // Show Description Details
    private void showDescriptionDetails() {
        containerDescriptionDetails.setVisibility(View.VISIBLE);
    }

    // Show Dosage Details
    private void showDosageDetails() {
        containerDosageDetails.setVisibility(View.VISIBLE);
    }

    // Show Manufacturer Details
    private void showManufacturerDetails() {
        containerManufacturerDetails.setVisibility(View.VISIBLE);
    }

    // Show Side Effects Details
    private void showSideEffectsDetails() {
        containerSideEffectsDetails.setVisibility(View.VISIBLE);
    }

    // Highlight the active tab
    private void highlightTab(TextView activeTab) {
        activeTab.setTextColor(getResources().getColor(R.color.active_tab_color)); // Active tab color
    }

    private void fetchDrugDetails() {
        String url = getString(R.string.api_drug_supply); // Your API URL for fetching drug details

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("DrugDetailsResponse", "Raw Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONObject drugDetails = jsonResponse.getJSONObject("drugDetails");

                                // Log the drug details
                                String quantity = drugDetails.getString("Quantity");
                                String brandName = drugDetails.getString("BrandName");
                                String genericName = drugDetails.getString("GenericName");
                                String activeIngredient = drugDetails.getString("Active_Ingredient");
                                String dosage = drugDetails.getString("Dosage");
                                String dosageForm = drugDetails.getString("Dosage_Form");
                                String manufacturer = drugDetails.getString("Manufacturer");
                                String manufactureDate = drugDetails.getString("Manufacture_Date");
                                String sideEffects = drugDetails.getString("SideEffects");
                                double price = drugDetails.getDouble("Price");
                                String imageUrl = getString(R.string.drug_image_url) + drugDetails.getString("DrugImage"); // Build image URL

                                // Log all the drug details
                                Log.d("DrugDetails", "Quantity: " + quantity);
                                Log.d("DrugDetails", "Brand Name: " + brandName);
                                Log.d("DrugDetails", "Generic Name: " + genericName);
                                Log.d("DrugDetails", "Active Ingredient: " + activeIngredient);
                                Log.d("DrugDetails", "Dosage: " + dosage);
                                Log.d("DrugDetails", "Dosage Form: " + dosageForm);
                                Log.d("DrugDetails", "Manufacturer: " + manufacturer);
                                Log.d("DrugDetails", "Manufacture Date: " + manufactureDate);
                                Log.d("DrugDetails", "Side Effects: " + sideEffects);
                                Log.d("DrugDetails", "Price: " + price);

                                // Set the values to the UI
                                ((TextView) findViewById(R.id.txtQuantity)).setText("Items: " + quantity);
                                ((TextView) findViewById(R.id.txtGenericName)).setText(genericName);
                                ((TextView)findViewById(R.id.txtPrice)).setText("RM " + String.valueOf(price));

                                // Load the image using Glide
                                Glide.with(ProductDesc.this)
                                        .load(imageUrl)  // The image URL
                                        .placeholder(R.drawable.default_profile_picture)  // Placeholder image
                                        .into((ImageView) findViewById(R.id.DrugImageView));

                                // Set the text values to other TextViews as well
                                ((TextView) findViewById(R.id.text_description_BrandName)).setText(brandName);
                                ((TextView) findViewById(R.id.text_description_GenericName)).setText(genericName);
                                ((TextView) findViewById(R.id.text_description_ActiveIngredient)).setText(activeIngredient);
                                ((TextView) findViewById(R.id.text_dosage_Dosage)).setText(dosage);
                                ((TextView) findViewById(R.id.text_dosage_DosageForm)).setText(dosageForm);
                                ((TextView) findViewById(R.id.text_manufacturer_Manufacturer)).setText(manufacturer);
                                ((TextView) findViewById(R.id.text_manufacturer_ManufactureDate)).setText(manufactureDate);
                                ((TextView) findViewById(R.id.text_side_effects)).setText(sideEffects);

                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(ProductDesc.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("DrugDetailsResponseError", "JSON Parsing error", e);
                            Toast.makeText(ProductDesc.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("DrugDetailsError", "Error occurred", error);
                        Toast.makeText(ProductDesc.this, "Error fetching drug details", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getDrugDetails");
                params.put("SupplyID", supplyID); // Send the SupplyID
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

}
