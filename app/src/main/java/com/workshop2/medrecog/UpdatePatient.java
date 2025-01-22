package com.workshop2.medrecog;

import android.content.Intent;
import android.graphics.Color;
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
    private ArrayList<String> selectedSymptoms;
    private ArrayList<String> selectedSymptomIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_patient);

        // Initialize views
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

        selectedSymptoms = new ArrayList<>();
        selectedSymptomIds = new ArrayList<>();

        // Get PatientID from Intent
        patientID = getIntent().getStringExtra("PatientID");
        Log.d("PatientID", patientID);

        // Fetch and populate patient information
        getPatientUpdate();

        // Save updated patient data
        buttonSave.setOnClickListener(v -> updatePatientInformation());

        // Set an OnClickListener to open a symptom selection activity
        symptomTextView.setOnClickListener(v -> {
            Intent symptomIntent = new Intent(UpdatePatient.this, SearchSymptom.class);
            startActivityForResult(symptomIntent, 1); // 1 is the request code for the symptom activity
        });

        // Handle back button behavior
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close the activity
            }
        });

        // Set an OnClickListener for the back button
        imageBack.setOnClickListener(v -> onBackPressed()); // Call the overridden onBackPressed method
    }

    private void getPatientUpdate() {
        String url = getString(R.string.api_patient);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
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
                },
                error -> Toast.makeText(UpdatePatient.this, "Error fetching patient data", Toast.LENGTH_SHORT).show()) {
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

    private void searchSymptom(String symptomIds) {
        String url = "https://www.etourmersing.com/BackEnd-APi/MedRec/api_symptom.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("API_Response", response);

                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            JSONArray symptomsArray = jsonResponse.getJSONArray("symptoms");

                            StringBuilder descriptions = new StringBuilder();
                            for (int i = 0; i < symptomsArray.length(); i++) {
                                JSONObject symptom = symptomsArray.getJSONObject(i);
                                String description = symptom.getString("Description");

                                descriptions.append(description);
                                if (i < symptomsArray.length() - 1) {
                                    descriptions.append(", "); // Separate descriptions with a comma
                                }
                            }
                            symptomTextView.setText(descriptions.toString()); // Display descriptions
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(UpdatePatient.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON_Parsing_Error", "Raw response: " + response, e);
                        Toast.makeText(UpdatePatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(UpdatePatient.this, "Error fetching symptom", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "searchSymptom");
                params.put("SymptomIDs", symptomIds);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void updatePatientInformation() {
        String url = getString(R.string.api_patient);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
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
                },
                error -> Toast.makeText(UpdatePatient.this, "Error updating patient", Toast.LENGTH_SHORT).show()) {
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
                params.put("symptomID", TextUtils.join(",", selectedSymptomIds));

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
            // Retrieve selected symptoms and their corresponding IDs
            ArrayList<String> selectedSymptomList = data.getStringArrayListExtra("selectedSymptoms");
            ArrayList<String> selectedSymptomIdList = data.getStringArrayListExtra("selectedSymptomIds");

            if (selectedSymptomList != null && selectedSymptomIdList != null) {
                selectedSymptoms = selectedSymptomList;
                selectedSymptomIds = selectedSymptomIdList;
                updateSymptomsDisplay();
            }
        }
    }

    private void updateSymptomsDisplay() {
        // Concatenate symptoms into a single string, separated by a comma or newline
        String displayText = TextUtils.join(", ", selectedSymptoms);
        symptomTextView.setText(displayText);

        // Set the symptom IDs in the corresponding TextView for reference
        String symptomIds = TextUtils.join(",", selectedSymptomIds);
        symptomIdTextView.setText(symptomIds);
    }
}
