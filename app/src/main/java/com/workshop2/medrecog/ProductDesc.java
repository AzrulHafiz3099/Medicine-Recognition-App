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

    private String supplyID,userID;
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
        userID = getIntent().getStringExtra("userID");

        // Log the received SupplyID
        if (supplyID != null) {
            Log.d("ProductDesc", "Received SupplyID: " + supplyID);
        } else {
            Log.d("ProductDesc", "No SupplyID received");
        }

        // Log the received userID
        if (userID != null) {
            Log.d("ProductDesc", "Received userID: " + userID);
        } else {
            Log.d("ProductDesc", "No userID received");
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

                                // Retrieve the DrugID and other drug details
                                String drugID = drugDetails.getString("DrugID");  // Capture the DrugID
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

                                // Log the drug details for debugging
                                Log.d("drugID", "Drug ID: " + drugID);
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
                                txtQuantity.setText("Items: " + quantity);
                                txtGenericName.setText(genericName);
                                txtPrice.setText("RM " + price);

                                // Load the image using Glide
                                Glide.with(ProductDesc.this)
                                        .load(imageUrl)  // The image URL
                                        .placeholder(R.drawable.default_profile_picture)  // Placeholder image
                                        .into(DrugImageView);

                                // Set the text values to other TextViews as well
                                text_description_BrandName.setText(brandName);
                                text_description_GenericName.setText(genericName);
                                text_description_ActiveIngredient.setText(activeIngredient);
                                text_dosage_Dosage.setText(dosage);
                                text_dosage_DosageForm.setText(dosageForm);
                                text_manufacturer_Manufacturer.setText(manufacturer);
                                text_manufacturer_ManufactureDate.setText(manufactureDate);
                                text_side_effects.setText(sideEffects);

                                // Pass the drugID to the "Add to Cart" logic
                                findViewById(R.id.container_button_add_to_cart).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (userID != null && supplyID != null && drugID != null) {
                                            // Remove "RM " from the price before parsing it
                                            String priceText = txtPrice.getText().toString().replace("RM ", "").trim();
                                            double price = Double.parseDouble(priceText);

                                            // Now, pass drugID to checkOrCreateCart
                                            checkOrCreateCart(userID, drugID, genericName, imageUrl, 1, price);
                                        } else {
                                            Toast.makeText(ProductDesc.this, "UserID, SupplyID, or DrugID is missing", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

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


    private void checkOrCreateCart(final String userID, final String drugID, final String genericName, final String drugImage, final int quantity, final double price) {
        String url = getString(R.string.api_cart);  // Your API URL

        Log.d("ProductDesc", "Checking or creating cart and adding item for UserID: " + userID);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("ProductDesc", "Response from check/create cart and add item: " + response); // Debugging line
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            // Either cart existed or was created
                            String cartID = jsonResponse.getString("CartID");
                            Log.d("ProductDesc", "Cart ID: " + cartID); // Debugging line
                            // Proceed to add the item to the cart
                            addToCart(cartID, drugID, genericName, drugImage, quantity, price);
                        } else {
                            Log.d("ProductDesc", "Failed to check/create cart: " + jsonResponse.getString("message"));
                            Toast.makeText(this, "Cart Created, please click Add To Cart Button again.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ProductDesc", "JSON Parsing error in check/create cart and add item", e);
                        Toast.makeText(this, "JSON Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ProductDesc", "Error occurred while checking/creating cart and adding item: " + error.getMessage(), error);
                    Toast.makeText(this, "Request error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "check_or_create_cart");
                params.put("UserID", userID);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void addToCart(final String cartID, final String drugID, final String genericName, final String drugImage, final int quantity, final double price) {
        String url = getString(R.string.api_cart_item);

        Log.d("ProductDesc", "Adding item to cart. CartID: " + cartID + ", DrugID: " + drugID);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("ProductDesc", "Response from adding item to cart: " + response); // Debugging line
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            Log.d("ProductDesc", "Item successfully added to cart"); // Debugging line
                            Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("ProductDesc", "Failed to add item to cart: " + jsonResponse.getString("message")); // Debugging line
                            Toast.makeText(this, "Cart Created, please click Add To Cart Button again.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ProductDesc", "JSON Parsing error in addToCart", e); // Debugging line
                        Toast.makeText(this, "JSON Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ProductDesc", "Error occurred while adding item to cart: " + error.getMessage(), error); // Debugging line
                    Toast.makeText(this, "Request error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_cart_item");
                params.put("CartID", cartID);
                params.put("DrugID", drugID);
                params.put("GenericName", genericName);
                params.put("DrugImage", drugImage);
                params.put("Quantity", "1");
                params.put("Price", String.valueOf(price));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }








}
