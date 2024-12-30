package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MedDetailsReminder extends AppCompatActivity {

    private String patientID, title, description, date, time, symptomID, drugID;
    private ImageView drugImage, imgBack;
    private TextView genericName, dosage, dosageForm, dosageUsage;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meddetailsreminder);

        drugImage = findViewById(R.id.img_drugImage);
        imgBack = findViewById(R.id.img_back);
        genericName = findViewById(R.id.txtGenericName);
        dosage = findViewById(R.id.txt_Dosage);
        dosageForm = findViewById(R.id.txt_DosageForm);
        dosageUsage = findViewById(R.id.txt_DosageUsage);
        btnAdd = findViewById(R.id.btnAdd);

        Intent intent = getIntent();
        patientID = intent.getStringExtra("patientID");
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        symptomID = intent.getStringExtra("symptomID");
        drugID = intent.getStringExtra("drugID");

        Log.d("MedDetailsReminder", "Patient ID: " + patientID);
        Log.d("MedDetailsReminder", "Title: " + title);
        Log.d("MedDetailsReminder", "Description: " + description);
        Log.d("MedDetailsReminder", "Date: " + date);
        Log.d("MedDetailsReminder", "Time: " + time);
        Log.d("MedDetailsReminder", "Symptom ID: " + symptomID);
        Log.d("MedDetailsReminder", "Drug ID: " + drugID);

        fetchDrugDetails(drugID);

        imgBack.setOnClickListener(v -> {
            Intent intent1 = new Intent(MedDetailsReminder.this, Addreminder.class);
            intent1.putExtra("patientID", patientID);
            intent1.putExtra("title", title);
            intent1.putExtra("description", description);
            intent1.putExtra("date", date);
            intent1.putExtra("time", time);
            intent1.putExtra("symptomID", symptomID);
            startActivity(intent1);
        });

        btnAdd.setOnClickListener(v -> addReminder());

    }

    private void fetchDrugDetails(String drugID) {
        String url = getString(R.string.api_drug_details);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            JSONObject drugDetails = jsonResponse.getJSONObject("drugDetails");

                            // Build the image URL
                            String imageUrl = getString(R.string.drug_image_url) + drugDetails.getString("DrugImage");
                            Log.d("imageurl", "imageurl: " + imageUrl);

                            String genericNameText = drugDetails.getString("GenericName");
                            String dosageText = drugDetails.getString("Dosage");
                            String dosageFormText = drugDetails.getString("Dosage_Form");
                            String dosageUsageText = drugDetails.getString("DosageUsage");

                            // Load the image using Glide
                            Glide.with(MedDetailsReminder.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_profile_picture) // Placeholder image
                                    .into(drugImage);

                            genericName.setText(genericNameText);
                            dosage.setText(dosageText);
                            dosageForm.setText(dosageFormText);
                            dosageUsage.setText(dosageUsageText);

                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(MedDetailsReminder.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MedDetailsReminder.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MedDetailsReminder.this, "Error fetching drug details", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getDrugDetails");
                params.put("DrugID", drugID);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void addReminder() {
        String url = getString(R.string.api_reminder); // Replace with your actual API URL

        // Convert the date to the required format
        String formattedDate = convertDateToDatabaseFormat(date);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            Toast.makeText(MedDetailsReminder.this, "Reminder added successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MedDetailsReminder.this, Drugreminder.class);
                            startActivity(intent);


                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(MedDetailsReminder.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MedDetailsReminder.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MedDetailsReminder.this, "Error adding reminder", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "addReminder");
                params.put("PatientID", patientID);
                params.put("Title", title);
                params.put("Description", description);
                params.put("GenericName", genericName.getText().toString());
                params.put("ReminderDate", formattedDate);
                params.put("ReminderTime", time);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private String convertDateToDatabaseFormat(String inputDate) {
        try {
            // Parse the input date
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(inputDate);

            // Format it for the database
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            // Return the original date if parsing fails
            return inputDate;
        }
    }


}
