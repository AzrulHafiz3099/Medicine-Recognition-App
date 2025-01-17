package com.workshop2.medrecog;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Find views by ID
        TextView medicineName = findViewById(R.id.medicineName);
        ImageView medicineImage = findViewById(R.id.medicineImage);
        TextView medicineDetails = findViewById(R.id.medicineDetails);

        // Get details from intent
        String detailsJson = getIntent().getStringExtra("details");

        if (detailsJson != null) {
            try {
                // Parse JSON
                JSONObject jsonObject = new JSONObject(detailsJson);
                String name = jsonObject.optString("name", "No Name Available");
                String description = jsonObject.optString("description", "No Description Available");
                String dosage = jsonObject.optString("dosage", "No Dosage Information");
                String manufacturer = jsonObject.optString("manufacturer", "Unknown Manufacturer");
                String sideEffects = jsonObject.optString("side_effects", "No Side Effects Listed");
                String imageUrl = jsonObject.optString("image_url", "");

                // Set values
                medicineName.setText(name);
                String details = "Description: " + description + "\n\n"
                        + "Dosage: " + dosage + "\n\n"
                        + "Manufacturer: " + manufacturer + "\n\n"
                        + "Side Effects: " + sideEffects;
                medicineDetails.setText(details);

                // Load image
                if (!imageUrl.isEmpty()) {
                    medicineImage.setVisibility(ImageView.VISIBLE);
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(medicineImage);
                } else {
                    medicineImage.setVisibility(ImageView.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
