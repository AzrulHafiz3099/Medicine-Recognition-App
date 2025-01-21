package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterPatient extends AppCompatActivity {

    private static final int SEARCH_SYMPTOM_REQUEST = 1;
    private String userID;
    private EditText inputName, inputAge, inputAddress, inputMedicalHistory, inputPhone;
    private RadioGroup genderGroup;
    private Button buttonSave;
    private TextView tvSelectSymptom, tvSelectSymptomId;
    private ArrayList<String> selectedSymptoms;
    private ArrayList<String> selectedSymptomIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_patient);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        Log.d("UserID", userID);

        // Initialize views
        inputName = findViewById(R.id.input_name);
        inputAge = findViewById(R.id.input_age);
        inputAddress = findViewById(R.id.input_address);
        inputMedicalHistory = findViewById(R.id.input_medical_history);
        inputPhone = findViewById(R.id.input_phone);
        genderGroup = findViewById(R.id.gender_group);
        buttonSave = findViewById(R.id.button_save);
        tvSelectSymptom = findViewById(R.id.symptom_select);
        tvSelectSymptomId = findViewById(R.id.symptom_select_id);
        selectedSymptoms = new ArrayList<>();
        selectedSymptomIds = new ArrayList<>();

        // Set click listener for symptom selection
        tvSelectSymptom.setOnClickListener(v -> {
            Log.d("TextViewClick", "TextView was clicked!");
            Intent intent1 = new Intent(RegisterPatient.this, SearchSymptom.class);
            startActivityForResult(intent1, SEARCH_SYMPTOM_REQUEST);
        });

        // Set onClickListener for the Save button
        buttonSave.setOnClickListener(v -> registerPatients());

        // Handle back button behavior
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_SYMPTOM_REQUEST && resultCode == RESULT_OK && data != null) {
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
        tvSelectSymptom.setText(displayText); // Display selected descriptions in the TextView

        // Concatenate symptom IDs into a single string, separated by a comma or newline
        String displayIdText = TextUtils.join(", ", selectedSymptomIds);
        tvSelectSymptomId.setText(displayIdText); // Display selected symptom IDs in the new TextView
    }

    private void registerPatients() {
        // Get user inputs
        String name = inputName.getText().toString().trim();
        String age = inputAge.getText().toString().trim();
        String address = inputAddress.getText().toString().trim();
        String medicalHistory = inputMedicalHistory.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        int selectedGenderId = genderGroup.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender != null ? selectedGender.getText().toString() : "";

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(address) ||
                TextUtils.isEmpty(medicalHistory) || TextUtils.isEmpty(phone) || selectedGenderId == -1 ||
                selectedSymptomIds.isEmpty()) {
            Toast.makeText(RegisterPatient.this, "Please fill in all fields and select symptoms", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare request parameters
        String url = getString(R.string.api_patient);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                Toast.makeText(RegisterPatient.this, "Patient registered successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterPatient.this, AddPatient.class);
                                startActivity(intent);
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(RegisterPatient.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("RegisterPatientResponseError", "JSON Parsing error", e);
                            Toast.makeText(RegisterPatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("RegisterPatientError", "Error occurred", error);
                        Toast.makeText(RegisterPatient.this, "Error registering patient", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "registerPatients");
                params.put("userID", userID);
                params.put("name", name);
                params.put("age", age);
                params.put("gender", gender);
                params.put("address", address);
                params.put("medicalHistory", medicalHistory);
                params.put("phone", phone);
                params.put("symptoms", TextUtils.join(",", selectedSymptomIds)); // Use symptom IDs instead of descriptions
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }
}
