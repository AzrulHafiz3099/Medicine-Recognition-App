package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class RegisterPatient extends AppCompatActivity {

    private String userID;
    private EditText inputName, inputAge, inputAddress, inputMedicalHistory, inputPhone;
    private RadioGroup genderGroup;
    private Button buttonSave;
    private Spinner symptomsSpinner;
    private ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_patient);

        symptomsSpinner = findViewById(R.id.symptom_spinner);
        imageBack = findViewById(R.id.img_back);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        Log.d("UserID", userID);

        // Initialize other views
        inputName = findViewById(R.id.input_name);
        inputAge = findViewById(R.id.input_age);
        inputAddress = findViewById(R.id.input_address);
        inputMedicalHistory = findViewById(R.id.input_medical_history);
        inputPhone = findViewById(R.id.input_phone);
        genderGroup = findViewById(R.id.gender_group);
        buttonSave = findViewById(R.id.button_save);

        // Set onClickListener for the Save button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPatients();
            }
        });

        // Set click listener on the image_icon
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to Homepage activity
                Intent intent = new Intent(RegisterPatient.this, AddPatient.class);
                startActivity(intent);
                finish(); // Optional: To close the activity if needed
            }
        });

        // Fetch symptoms from the API
        fetchSymptoms();
    }

    private void fetchSymptoms() {
        String url = getString(R.string.api_symptom); // Define the correct API URL in your strings.xml

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("SymptomsResponse", "Raw Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONArray symptomsArray = jsonResponse.getJSONArray("symptoms");

                                // Create a list to hold the symptom names and IDs
                                final List<Symptom> symptomsList = new ArrayList<>();

                                // Add the default "Select a symptom" item at the start
                                symptomsList.add(new Symptom("", "Select a symptom"));

                                for (int i = 0; i < symptomsArray.length(); i++) {
                                    JSONObject symptomObject = symptomsArray.getJSONObject(i);
                                    String symptomID = symptomObject.getString("SymptomID");
                                    String symptomDescription = symptomObject.getString("Description");
                                    symptomsList.add(new Symptom(symptomID, symptomDescription));
                                }

                                // Create an ArrayAdapter for the spinner with custom objects (Symptom)
                                ArrayAdapter<Symptom> adapter = new ArrayAdapter<>(RegisterPatient.this,
                                        android.R.layout.simple_spinner_item, symptomsList) {
                                    @Override
                                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getDropDownView(position, convertView, parent);
                                        TextView textView = (TextView) view;
                                        textView.setText(symptomsList.get(position).getDescription());
                                        return view;
                                    }
                                };

                                // Specify the layout to use when the list of choices appears
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                // Apply the adapter to the spinner
                                symptomsSpinner.setAdapter(adapter);

                                symptomsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                        Symptom selectedSymptom = symptomsList.get(position);
                                        String selectedSymptomID = selectedSymptom.getSymptomID();
                                        String selectedSymptomDescription = selectedSymptom.getDescription();

                                        // You can use the selectedSymptomID here for further operations
                                        Log.d("Selected SymptomID", selectedSymptomID);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parentView) {
                                        // Handle case when nothing is selected
                                    }
                                });

                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(RegisterPatient.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("SymptomsResponseError", "JSON Parsing error", e);
                            Toast.makeText(RegisterPatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SymptomsError", "Error occurred", error);
                        Toast.makeText(RegisterPatient.this, "Error fetching symptoms", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getSymptoms"); // The action is "getSymptoms"
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
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

        // Get selected symptom ID from the spinner
        Symptom selectedSymptom = (Symptom) symptomsSpinner.getSelectedItem();
        String symptomID = selectedSymptom != null ? selectedSymptom.getSymptomID() : "";
        Log.d("RegisterResponse", "Symptom ID: " + symptomID);

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(address) ||
                TextUtils.isEmpty(medicalHistory) || TextUtils.isEmpty(phone) || selectedGenderId == -1 || TextUtils.isEmpty(symptomID) || symptomID.equals("")) {
            Toast.makeText(RegisterPatient.this, "Please fill in all fields and select a symptom", Toast.LENGTH_SHORT).show();
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
                params.put("symptomID", symptomID);  // Send the symptomID here
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }


}