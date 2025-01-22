package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;


public class Suggestion extends AppCompatActivity {
    private TextView medicineName, medicineDetails;
    private ImageView medicineImage;
    private RequestQueue requestQueue;
    private View medicineDetailsCard;
    private ImageView imageBack;
    private Button buyNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestion);

        // Initialize views
        medicineName = findViewById(R.id.medicineName);
        medicineDetails = findViewById(R.id.medicineDetails);
        medicineImage = findViewById(R.id.medicineImage);
        medicineDetailsCard = findViewById(R.id.medicineDetailsCard);
        buyNow = findViewById(R.id.text_buy_now);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Get the selected medicine name and drug ID from the Intent
        String selectedMedicine = getIntent().getStringExtra("medicineName");
        String drugId = getIntent().getStringExtra("drugId");
        Log.d("SuggestionActivity", "Selected Medicine: " + selectedMedicine);
        Log.d("SuggestionActivity", "Drug ID: " + drugId);

        if (drugId != null) {
            fetchMedicineDetails(drugId); // Fetch the details using drugId
        } else {
            Toast.makeText(this, "Error: No drug ID found", Toast.LENGTH_SHORT).show();
        }

        imageBack = findViewById(R.id.img_back); // Find the imageIcon

        // Handle back button behavior with the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close the activity
            }
        });

        imageBack.setOnClickListener(v -> onBackPressed());
        buyNow.setOnClickListener(view -> {
            Intent intent2 = new Intent(Suggestion.this, Homepage.class);
            startActivity(intent2);
        });

    }


    private void fetchMedicineDetails(String drugId) {
        // Use the drugId to build the URL for fetching medicine details
        String url = getString(R.string.api_search) + "?drugId=" + drugId; // Make sure the query parameter is drugId
        Log.d("API URL", "URL: " + url);


        medicineDetailsCard.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("API Response", "Response: " + response.toString());

                            // Set medicine name
                            String name = response.getString("BrandName");
                            medicineName.setText(name);

                            // Set medicine details
                            String details = "Generic Name: " + response.getString("GenericName") + "\n" +
                                    "Dosage: " + response.getString("Dosage") + "\n" +
                                    "Manufacturer: " + response.getString("Manufacturer") + "\n" +
                                    "Side Effects: " + response.getString("SideEffects");
                            medicineDetails.setText(details);

                            // Load the image using Glide
                            Glide.with(Suggestion.this)
                                    .load(getString(R.string.drug_image_url) + response.getString("DrugImage"))
                                    .into(medicineImage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            medicineDetails.setText("Error fetching details");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        medicineDetails.setText("Error fetching details");
                    }
                });

        requestQueue.add(request);
    }

}