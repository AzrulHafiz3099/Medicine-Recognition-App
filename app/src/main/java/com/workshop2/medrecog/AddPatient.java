package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
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

public class AddPatient extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private List<Patient> patientList;
    private String userID;
    private Button btnAddNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_patient);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        patientList = new ArrayList<>();
        patientAdapter = new PatientAdapter(this, patientList);
        recyclerView.setAdapter(patientAdapter);

        userID = "US_0002";
        // Sample data
        fetchPatients();

        // Set up button click listener
        Button btnAddNew = findViewById(R.id.btn_add_new);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to the next activity
                Intent intent = new Intent(AddPatient.this, RegisterPatient.class);
                intent.putExtra("userID", userID);  // Passing userID to the next activity
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if you received updated data from UpdatePatient
        Intent intent = getIntent();
        String updatedPatientID = intent.getStringExtra("updatedPatientID");

        if (updatedPatientID != null) {
            // Reload your data or refresh the list
            // e.g., call the method to refresh the RecyclerView data
            fetchPatients();
        }

    }

    private void fetchPatients() {
        String url = getString(R.string.api_patient);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("PatientsResponse", "Raw Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONArray patients = jsonResponse.getJSONArray("patients");

                                // Clear existing list to avoid duplicates
                                patientList.clear();

                                for (int i = 0; i < patients.length(); i++) {
                                    JSONObject patientObj = patients.getJSONObject(i);

                                    String patientID = patientObj.getString("PatientID");
                                    String medicalHistory = patientObj.getString("MedicalHistory");
                                    String phoneNumber = patientObj.getString("Phonenumber");
                                    String name = patientObj.getString("Name");
                                    String gender = patientObj.getString("Gender");
                                    String address = patientObj.getString("Address");
                                    String ageString = patientObj.getString("Age");

                                    // Try to parse age as integer
                                    int age = 0;
                                    try {
                                        age = Integer.parseInt(ageString);
                                    } catch (NumberFormatException e) {
                                        Log.e("PatientsResponseError", "Invalid age format: " + ageString, e);
                                        age = -1;  // You could set a default or error value here
                                    }

                                    patientList.add(new Patient(patientID, name, age, gender, address, medicalHistory, phoneNumber));
                                }

                                // Notify the adapter about data changes
                                patientAdapter.notifyDataSetChanged();
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(AddPatient.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("PatientsResponseError", "JSON Parsing error", e);
                            Toast.makeText(AddPatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PatientsError", "Error occurred", error);
                        Toast.makeText(AddPatient.this, "Error fetching patient data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getPatients");
                params.put("userID", userID);
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

}