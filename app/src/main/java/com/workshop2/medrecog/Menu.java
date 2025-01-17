package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Menu extends AppCompatActivity {

    private LinearLayout container_patient, container_drugReminder, container_symptomsDetector, container_drugSearch, container_medRecognition, container_vendor, container_yourOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        container_drugReminder = findViewById(R.id.container_drugReminder);
        container_symptomsDetector = findViewById(R.id.container_symptomsDetector);
        container_drugSearch = findViewById(R.id.container_drugSearch);
        container_medRecognition = findViewById(R.id.container_medRecognition);
        container_vendor = findViewById(R.id.container_vendor);
        container_yourOrder = findViewById(R.id.container_yourOrder);
        container_patient = findViewById(R.id.container_patient);

        // Set click listeners for each LinearLayout
        container_patient.setOnClickListener(view -> {
            Log.d("MenuActivity", "Drug Reminder clicked");
            Intent intent = new Intent(Menu.this, AddPatient.class);
            startActivity(intent);
        });

        // Set click listeners for each LinearLayout
        container_drugReminder.setOnClickListener(view -> {
            Log.d("MenuActivity", "Drug Reminder clicked");
            Intent intent = new Intent(Menu.this, Drugreminder.class);
            startActivity(intent);
        });

        container_symptomsDetector.setOnClickListener(view -> {
//            Intent intent = new Intent(Menu.this, SymptomsDetectorActivity.class);
//            startActivity(intent);
        });

        container_drugSearch.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, Search.class);
            startActivity(intent);
        });

        container_medRecognition.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, MedicineReco.class);
            startActivity(intent);
        });

        container_vendor.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, StoreSelect.class);
            startActivity(intent);
        });

        container_yourOrder.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, OrderList.class);
            startActivity(intent);
        });

    }
}