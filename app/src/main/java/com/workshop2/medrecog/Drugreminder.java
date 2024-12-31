package com.workshop2.medrecog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Drugreminder extends AppCompatActivity {

    private TextView dateTextView, timeTextView;
    private EditText edtTextTitle, edtTextDesc;
    private Button buttonNext;
    private String userID, symptomID, name;
    private Spinner patientSpinner;

    // List to store patient names and full patient data
    private List<Patient> patientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drugreminder);

        userID = "US_0002";  // Replace with the actual user ID

        // Initialize the TextViews
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);
        patientSpinner = findViewById(R.id.patient_spinner);
        edtTextTitle = findViewById(R.id.edtTextTitle);
        edtTextDesc = findViewById(R.id.edtTextDesc);
        buttonNext = findViewById(R.id.button_next);

        // Initialize a Calendar instance
        Calendar calendar = Calendar.getInstance();

        // Set default date and time
        updateDate(calendar);
        updateTime(calendar);

        // Fetch patient data
        fetchPatients();

        // Set OnClickListener for Date TextView
        dateTextView.setOnClickListener(v -> {
            Calendar currentDate = Calendar.getInstance();
            new DatePickerDialog(Drugreminder.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        updateDate(calendar);
                    },
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    currentDate.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Set OnClickListener for Time TextView
        timeTextView.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            new TimePickerDialog(Drugreminder.this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateTime(calendar);
                    },
                    currentTime.get(Calendar.HOUR_OF_DAY),
                    currentTime.get(Calendar.MINUTE),
                    false // Use 12-hour format
            ).show();
        });

        buttonNext.setOnClickListener(v -> {

            // Validate that all fields are filled
            if (patientSpinner.getSelectedItem() == null) {
                Toast.makeText(Drugreminder.this, "Please select a patient", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = edtTextTitle.getText().toString().trim();
            String description = edtTextDesc.getText().toString().trim();
            String date = dateTextView.getText().toString().trim();
            String time = timeTextView.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(Drugreminder.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the selected patient data
            Patient selectedPatient = (Patient) patientSpinner.getSelectedItem();
            String patientID = selectedPatient != null ? selectedPatient.getPatientID() : "";
            String patientName = selectedPatient != null ? selectedPatient.getName() : "";
            Log.d("Drugreminder", "Patient ID in drug reminder : " + patientID);
            Log.d("Drugreminder", "Patient Name in drug reminder: " + patientName);


            // Create the intent and pass the data to the next activity
            Intent intent = new Intent(Drugreminder.this, Addreminder.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("name", patientName);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("symptomID", symptomID);

            // Start the Addreminder activity
            startActivity(intent);
        });

    }

    // Method to fetch patients data from API
    private void fetchPatients() {
        String url = getString(R.string.api_patient);  // Use your actual API endpoint

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
                                    name = patientObj.getString("Name");
                                    int age = patientObj.getInt("Age");
                                    String gender = patientObj.getString("Gender");
                                    String address = patientObj.getString("Address");
                                    String medicalHistory = patientObj.getString("MedicalHistory");
                                    symptomID = patientObj.getString("SymptomID");
                                    String phoneNumber = patientObj.getString("Phonenumber");

                                    Patient patient = new Patient(patientID, name, age, gender, address, medicalHistory, phoneNumber);
                                    patientList.add(patient);
                                }

                                // Create an adapter for the spinner and set it
                                PatientAdapter adapter = new PatientAdapter(Drugreminder.this, patientList);
                                patientSpinner.setAdapter(adapter);

                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(Drugreminder.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("PatientsResponseError", "JSON Parsing error", e);
                            Toast.makeText(Drugreminder.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PatientsError", "Error occurred", error);
                        Toast.makeText(Drugreminder.this, "Error fetching patient data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getPatients");
                params.put("userID", userID);  // Send the userID to filter patients
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Update the date TextView
    private void updateDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(calendar.getTime()));
    }

    // Update the time TextView
    private void updateTime(Calendar calendar) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        timeTextView.setText(timeFormat.format(calendar.getTime()));
    }

    // Custom Adapter to display only patient names in the spinner
    public class PatientAdapter extends ArrayAdapter<Patient> {

        public PatientAdapter(Context context, List<Patient> patients) {
            super(context, android.R.layout.simple_spinner_item, patients);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public Patient getItem(int position) {
            return super.getItem(position); // Return the full Patient object, not just the name
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Customize the display of the selected item (patient's name)
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView nameTextView = convertView.findViewById(android.R.id.text1);
            nameTextView.setText(getItem(position).getName()); // Set the patient's name

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // Customize the dropdown list view (patient's name)
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            TextView nameTextView = convertView.findViewById(android.R.id.text1);
            nameTextView.setText(getItem(position).getName()); // Set the patient's name for dropdown

            return convertView;
        }
    }

}

