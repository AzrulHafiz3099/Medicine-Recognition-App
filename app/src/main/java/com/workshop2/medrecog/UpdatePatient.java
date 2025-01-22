package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Map;

public class UpdatePatient extends AppCompatActivity {

    private String patientID;
    private EditText inputName, inputAge, inputAddress, inputMedicalHistory, inputPhone;
    private TextView symptomTextView, symptomIdTextView;
    private Button buttonSave;
    private RadioGroup genderGroup;
    private ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_patient);

        inputName = findViewById(R.id.input_name);
        inputAge = findViewById(R.id.input_age);
        inputAddress = findViewById(R.id.input_address);
        inputMedicalHistory = findViewById(R.id.input_medical_history);
        inputPhone = findViewById(R.id.input_phone);
        symptomTextView = findViewById(R.id.symptom_select);
        symptomIdTextView = findViewById(R.id.symptom_select_id);
        buttonSave = findViewById(R.id.button_save);
        genderGroup = findViewById(R.id.gender_group);
        imageBack = findViewById(R.id.img_back);

        // Get PatientID from Intent
        patientID = getIntent().getStringExtra("PatientID");
        Log.d("PatientID", patientID);

        // Fetch and populate patient information
        getPatientUpdate();


        // Save updated patient data
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePatientInformation();
            }
        });

        // Set an OnClickListener to open a symptom selection activity
        symptomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent symptomIntent = new Intent(UpdatePatient.this, SearchSymptom.class);
                startActivityForResult(symptomIntent, 1); // 1 is the request code for the symptom activity
            }
        });

        // Handle back button behavior
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close the activity
            }
        });

        // Handle back button behavior with the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom logic for back press
                finish(); // Close the activity
            }
        });

        // Set an OnClickListener for the back button
        imageBack.setOnClickListener(v -> {
            onBackPressed(); // Call the overridden onBackPressed method
        });

    }

    private void getPatientUpdate() {
        String url = getString(R.string.api_patient);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONObject patient = jsonResponse.getJSONObject("patient");

                                // Populate the input fields
                                inputName.setText(patient.getString("Name"));
                                inputAge.setText(String.valueOf(patient.getInt("Age")));
                                inputAddress.setText(patient.getString("Address"));
                                inputMedicalHistory.setText(patient.getString("MedicalHistory"));
                                inputPhone.setText(patient.getString("Phonenumber"));

                                // Set the gender RadioButton
                                String gender = patient.getString("Gender");
                                if ("Male".equalsIgnoreCase(gender)) {
                                    ((RadioButton) findViewById(R.id.male)).setChecked(true);
                                } else if ("Female".equalsIgnoreCase(gender)) {
                                    ((RadioButton) findViewById(R.id.female)).setChecked(true);
                                }

                                // Populate the symptom TextView
                                //symptomTextView.setText(patient.getString("Description"));
                                // After setting the SymptomID to symptomIdTextView, call searchSymptom
                                symptomIdTextView.setText(patient.getString("SymptomID"));
                                String symptomId = patient.getString("SymptomID");

                                // Call searchSymptom with SymptomID
                                searchSymptom(symptomId);

                            } else {
                                Toast.makeText(UpdatePatient.this, "Error fetching patient data", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(UpdatePatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UpdatePatient.this, "Error fetching patient data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getPatientsByID");
                params.put("patientID", patientID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /*private void fetchSymptoms() {
        String url = getString(R.string.api_symptom);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONArray symptomsArray = jsonResponse.getJSONArray("symptoms");

                                if (symptomsArray.length() > 0) {
                                    // Assuming you want to display the first symptom in the TextViews
                                    JSONObject symptom = symptomsArray.getJSONObject(0);

                                    // Get data from the first symptom object
                                    String description = symptom.getString("Description");
                                    String symptomID = symptom.getString("SymptomID");

                                    // Update the TextViews
                                    symptomTextView.setText(description);
                                    symptomIdTextView.setText(symptomID);
                                } else {
                                    // No symptoms found, show a default message
                                    symptomTextView.setText("No symptoms available");
                                    symptomIdTextView.setText("N/A");
                                }
                            } else {
                                Toast.makeText(UpdatePatient.this, "Error fetching symptoms", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(UpdatePatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UpdatePatient.this, "Error fetching symptoms", Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }*/

    private void searchSymptom(String symptomId) {
        // Corrected URL (no query parameters in URL)
        //String url = "http://192.168.0.16/BackEnd-APi/MedRec/api_symptom.php";
        String url = "http://10.131.77.114/BackEnd-APi/MedRec/api_symptom.php";

        // Create a new StringRequest
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Log the response to check its content
                            Log.d("API_Response", response); // Add this line to log the response

                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONObject symptom = jsonResponse.getJSONObject("symptom");

                                // Get and display the symptom details
                                String description = symptom.getString("Description");
                                String id = symptom.getString("SymptomID");

                                symptomTextView.setText(description);  // Set the description to symptomTextView
                                symptomIdTextView.setText(id);  // Optionally, set the ID to symptomIdTextView
                            } else {
                                // Show error message from API
                                String message = jsonResponse.getString("message");
                                Toast.makeText(UpdatePatient.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            // Handle JSON parsing error
                            Toast.makeText(UpdatePatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle Volley error
                        Toast.makeText(UpdatePatient.this, "Error fetching symptom", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Pass parameters to the PHP API
                Map<String, String> params = new HashMap<>();
                params.put("action", "searchSymptom");
                params.put("SymptomID", symptomId);  // Use the symptomId
                return params;
            }
        };

        // Add the request to the Volley RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void updatePatientInformation() {
        String url = getString(R.string.api_patient);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                Toast.makeText(UpdatePatient.this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                                finish(); // Close activity after saving
                            } else {
                                Toast.makeText(UpdatePatient.this, "Failed to update patient", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(UpdatePatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UpdatePatient.this, "Error updating patient", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updatePatient");
                params.put("patientID", patientID);
                params.put("name", inputName.getText().toString());
                params.put("age", inputAge.getText().toString());
                params.put("address", inputAddress.getText().toString());
                params.put("medicalHistory", inputMedicalHistory.getText().toString());
                params.put("phone", inputPhone.getText().toString());

                // Gender selection
                int selectedGenderId = genderGroup.getCheckedRadioButtonId();
                RadioButton selectedGender = findViewById(selectedGenderId);
                params.put("gender", selectedGender.getText().toString());

                // Symptoms
                params.put("symptomID", symptomIdTextView.getText().toString());

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Retrieve selected symptoms
            String selectedSymptoms = data.getStringExtra("selectedSymptoms");
            String selectedSymptomIDs = data.getStringExtra("selectedSymptomIDs");

            // Display them in the TextViews
            symptomTextView.setText(selectedSymptoms);
            symptomIdTextView.setText(selectedSymptomIDs);
        }
    }
}
