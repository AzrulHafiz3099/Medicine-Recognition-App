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
import java.util.List;
import java.util.Map;

public class UpdatePatient extends AppCompatActivity {

    private String patientID;
    private EditText inputName, inputAge, inputAddress, inputMedicalHistory, inputPhone;
    private Spinner symptomSpinner;
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
        symptomSpinner = findViewById(R.id.symptom_spinner);
        buttonSave = findViewById(R.id.button_save);
        genderGroup = findViewById(R.id.gender_group);
        imageBack = findViewById(R.id.img_back);

        // Get PatientID from Intent
        patientID = getIntent().getStringExtra("PatientID");
        Log.d("PatientID", patientID);

        // Fetch and populate patient information
        GetPatientsUpdate();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePatientInformation();
            }
        });

        // Set click listener on the image_icon
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to Homepage activity
                Intent intent = new Intent(UpdatePatient.this, AddPatient.class);
                startActivity(intent);
                finish(); // Optional: To close the activity if needed
            }
        });

    }

    private void GetPatientsUpdate() {
        String url = getString(R.string.api_patient); // Replace with your API URL
        Log.d("PatientID", patientID);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("PatientResponse", "Raw Response: " + response);

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

                                // Set the symptomSpinner
                                String symptomID = patient.getString("SymptomID");
                                fetchSymptoms(symptomID);

                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(UpdatePatient.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("PatientResponseError", "JSON Parsing error", e);
                            Toast.makeText(UpdatePatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PatientError", "Error occurred", error);
                        Toast.makeText(UpdatePatient.this, "Error fetching patient data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getPatientsByID"); // Ensure your PHP API supports this action
                params.put("patientID", patientID);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /**
     * Fetch and populate the symptoms list for the spinner.
     * @param selectedSymptomID The ID of the symptom to select.
     */
    private void fetchSymptoms(final String selectedSymptomID) {
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
                                ArrayAdapter<Symptom> adapter = new ArrayAdapter<>(UpdatePatient.this,
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
                                symptomSpinner.setAdapter(adapter);

                                // Set the spinner selection to the selected symptom
                                setSpinnerSelection(symptomSpinner, selectedSymptomID);

                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(UpdatePatient.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("SymptomsResponseError", "JSON Parsing error", e);
                            Toast.makeText(UpdatePatient.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SymptomsError", "Error occurred", error);
                        Toast.makeText(UpdatePatient.this, "Error fetching symptoms", Toast.LENGTH_SHORT).show();
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

    /**
     * Helper method to set the Spinner selection.
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Symptom symptom = (Symptom) spinner.getItemAtPosition(i);
            if (symptom.getSymptomID().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updatePatientInformation() {
        final String name = inputName.getText().toString().trim();
        final String age = inputAge.getText().toString().trim();
        final String address = inputAddress.getText().toString().trim();
        final String medicalHistory = inputMedicalHistory.getText().toString().trim();
        final String phone = inputPhone.getText().toString().trim();

        // Get the selected gender
        int selectedGenderID = genderGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderID);
        final String gender = selectedGenderButton != null ? selectedGenderButton.getText().toString() : "";

        // Get the selected symptom ID from the spinner
        final Symptom selectedSymptom = (Symptom) symptomSpinner.getSelectedItem();
        final String symptomID = selectedSymptom.getSymptomID();

        // Validate fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(address) ||
                TextUtils.isEmpty(medicalHistory) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(gender) ||
                symptomID.isEmpty()) {
            Toast.makeText(UpdatePatient.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the request
        String url = getString(R.string.api_patient); // Update the URL accordingly
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("UpdateResponse", "Raw Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                Toast.makeText(UpdatePatient.this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                                // Redirect back to previous screen or show a success message
                                Intent intent = new Intent(UpdatePatient.this, AddPatient.class);
                                intent.putExtra("updatedPatientID", patientID); // Pass the updated ID or necessary data
                                startActivity(intent);
                                finish();
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(UpdatePatient.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("UpdateResponseError", "JSON Parsing error", e);
                            Toast.makeText(UpdatePatient.this, "Error updating patient", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("UpdateError", "Error occurred", error);
                        Toast.makeText(UpdatePatient.this, "Error updating patient", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updatePatient"); // Ensure your PHP API supports this action
                params.put("patientID", patientID);
                params.put("name", name);
                params.put("age", age);
                params.put("gender", gender);
                params.put("address", address);
                params.put("medicalHistory", medicalHistory);
                params.put("phone", phone);
                params.put("symptomID", symptomID);

                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}
