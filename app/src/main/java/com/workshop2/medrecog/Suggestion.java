package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestion);

        // Initialize views
        medicineName = findViewById(R.id.medicineName);
        medicineDetails = findViewById(R.id.medicineDetails);
        medicineImage = findViewById(R.id.medicineImage);
        medicineDetailsCard = findViewById(R.id.medicineDetailsCard);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Get the selected medicine name from the Intent
        String selectedMedicine = getIntent().getStringExtra("medicineName");

        if (selectedMedicine != null) {
            fetchMedicineDetails(selectedMedicine); // Fetch the details
        } else {
            Toast.makeText(this, "Error: No medicine selected", Toast.LENGTH_SHORT).show();
        }

        // 1. Find the LinearLayout by its ID
        ImageView arrowback = findViewById(R.id.backButton);

        // 2. Set the click listener using a lambda expression
        arrowback.setOnClickListener(v -> {
            // This code will execute when the LinearLayout is clicked
            Intent intent = new Intent(Suggestion.this, Search.class);
            startActivity(intent);
        });
    }

    private void fetchMedicineDetails(String medicineName) {
        String url = getString(R.string.api_search) + medicineName;
        medicineDetailsCard.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Use local variable directly for text setting
                            TextView medicineNameTextView = findViewById(R.id.medicineName); // Local TextView
                            String name = response.getString("BrandName");
                            medicineNameTextView.setText(name);  // Use local TextView

                            String details = "Generic Name: " + response.getString("GenericName") + "\n" +
                                    "Dosage: " + response.getString("Dosage") + "\n" +
                                    "Manufacturer: " + response.getString("Manufacturer") + "\n" +
                                    "Side Effects: " + response.getString("SideEffects");
                            TextView medicineDetailsTextView = findViewById(R.id.medicineDetails);  // Local TextView
                            medicineDetailsTextView.setText(details);  // Use local TextView

                            // Load the image with Glide
                            ImageView medicineImage = findViewById(R.id.medicineImage); // Local ImageView
                            Glide.with(Suggestion.this)
                                    .load("http://192.168.0.16/BackEnd-APi/MedRec/DrugImage/" + response.getString("DrugImage"))
                                    .into(medicineImage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            TextView medicineDetailsTextView = findViewById(R.id.medicineDetails);
                            medicineDetailsTextView.setText("Error fetching details");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        TextView medicineDetailsTextView = findViewById(R.id.medicineDetails);
                        medicineDetailsTextView.setText("Error fetching details");
                    }
                });

        requestQueue.add(request);
    }
}