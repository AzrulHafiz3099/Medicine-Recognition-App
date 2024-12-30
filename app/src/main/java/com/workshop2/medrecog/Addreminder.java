package com.workshop2.medrecog;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Addreminder extends AppCompatActivity {

    private String patientID, title, description, date, time, symptomID;
    private Spinner spinnerDrug;
    private Button buttonNext;
    private List<String> drugIDs = new ArrayList<>();
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addreminder);

        spinnerDrug = findViewById(R.id.spinner_drug);
        buttonNext = findViewById(R.id.button_next);
        imgBack = findViewById(R.id.img_back);

        Intent intent = getIntent();
        patientID = intent.getStringExtra("patientID");
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        symptomID = intent.getStringExtra("symptomID");

        Log.d("AddReminder", "Patient ID: " + patientID);
        Log.d("AddReminder", "Title: " + title);
        Log.d("AddReminder", "Description: " + description);
        Log.d("AddReminder", "Date: " + date);
        Log.d("AddReminder", "Time: " + time);
        Log.d("AddReminder", "Symptom ID: " + symptomID);

        fetchDrugs(symptomID);

        buttonNext.setOnClickListener(v -> {
            int selectedPosition = spinnerDrug.getSelectedItemPosition();
            if (selectedPosition == 0) {
                // User selected the placeholder
                Toast.makeText(Addreminder.this, "Please select a valid medicine.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedDrugID = drugIDs.get(selectedPosition - 1); // Adjusting index for placeholder

            // Navigate to MedDetailsReminder.java
            Intent medDetailsIntent = new Intent(Addreminder.this, MedDetailsReminder.class);
            medDetailsIntent.putExtra("patientID", patientID);
            medDetailsIntent.putExtra("title", title);
            medDetailsIntent.putExtra("description", description);
            medDetailsIntent.putExtra("date", date);
            medDetailsIntent.putExtra("time", time);
            medDetailsIntent.putExtra("symptomID", symptomID);
            medDetailsIntent.putExtra("drugID", selectedDrugID);
            startActivity(medDetailsIntent);
        });

        imgBack.setOnClickListener(v -> {
            Intent intent1 = new Intent(Addreminder.this, Drugreminder.class);
            startActivity(intent1);
        });
    }

    private void fetchDrugs(String symptomID) {
        String url = getString(R.string.api_drug_header);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            JSONArray drugsArray = jsonResponse.getJSONArray("drugs");

                            List<String> genericNames = new ArrayList<>();
                            genericNames.add("Search for medicine"); // Placeholder item

                            for (int i = 0; i < drugsArray.length(); i++) {
                                JSONObject drug = drugsArray.getJSONObject(i);
                                String genericName = drug.getString("GenericName");
                                String drugID = drug.getString("DrugID");
                                genericNames.add(genericName);
                                drugIDs.add(drugID);
                                Log.d("AddReminder", "Drug ID: " + drugID);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Addreminder.this,
                                    android.R.layout.simple_spinner_item, genericNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerDrug.setAdapter(adapter);

                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(Addreminder.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(Addreminder.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(Addreminder.this, "Error fetching drug data", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getDrugsBySymptomID");
                params.put("symptomID", symptomID);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
