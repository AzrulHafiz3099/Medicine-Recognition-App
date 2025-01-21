package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchSymptom extends AppCompatActivity {

    private EditText editSearchSymptom;
    private ListView suggestionsList;
    private Button btnAddSymptom, btnSubmitSymptoms;
    private TextView selectedSymptomTextView;
    private ArrayAdapter<String> suggestionAdapter;
    private ArrayList<String> suggestions;
    private ArrayList<String> selectedSymptoms;
    private RequestQueue requestQueue;

    // Store both ID and Description in a Map
    private Map<String, String> selectedSymptomMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_symptom);

        // Initialize views
        editSearchSymptom = findViewById(R.id.edit_search_symptom);
        suggestionsList = findViewById(R.id.recyclerViewSuggestions);
        btnAddSymptom = findViewById(R.id.button_add_symptoms);
        btnSubmitSymptoms = findViewById(R.id.button_submit_symptoms);
        selectedSymptomTextView = findViewById(R.id.selected_symptom);

        // Initialize symptoms lists and map
        suggestions = new ArrayList<>();
        selectedSymptoms = new ArrayList<>();
        selectedSymptomMap = new HashMap<>();
        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestions);
        suggestionsList.setAdapter(suggestionAdapter);

        // Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // TextWatcher for search query updates
        editSearchSymptom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    suggestions.clear();
                    suggestionAdapter.notifyDataSetChanged();
                    suggestionsList.setVisibility(View.GONE);
                } else {
                    fetchSuggestions(query);
                    suggestionsList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle adding selected symptom to the list
        btnAddSymptom.setOnClickListener(v -> {
            String symptom = editSearchSymptom.getText().toString().trim();
            if (!symptom.isEmpty() && !selectedSymptoms.contains(symptom)) {
                selectedSymptoms.add(symptom);
                updateSelectedSymptomsTextView(); // Update the display of selected symptoms
                Toast.makeText(SearchSymptom.this, "Symptom added: " + symptom, Toast.LENGTH_SHORT).show();
                editSearchSymptom.setText(""); // Clear input field
            } else {
                Toast.makeText(SearchSymptom.this, "Symptom already added or invalid", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle submitting symptoms
        btnSubmitSymptoms.setOnClickListener(v -> {
            if (!selectedSymptomMap.isEmpty()) {
                // Collect the selected symptoms in the map
                ArrayList<String> selectedSymptomDetails = new ArrayList<>();
                ArrayList<String> selectedSymptomIds = new ArrayList<>();
                for (Map.Entry<String, String> entry : selectedSymptomMap.entrySet()) {
                    selectedSymptomDetails.add(entry.getValue()); // Description
                    selectedSymptomIds.add(entry.getKey()); // ID
                }

                // Send selected symptoms back
                Intent resultIntent = new Intent();
                resultIntent.putStringArrayListExtra("selectedSymptoms", selectedSymptomDetails); // Send descriptions
                resultIntent.putStringArrayListExtra("selectedSymptomIds", selectedSymptomIds); // Send IDs
                setResult(RESULT_OK, resultIntent); // Return result to MainActivity
                finish(); // Close SearchSymptom activity
            } else {
                Toast.makeText(SearchSymptom.this, "No symptoms selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up item click listener for suggestions
        suggestionsList.setOnItemClickListener((parent, view, position, id) -> {
            // When a suggestion is clicked, fill the EditText with the clicked symptom
            String clickedSymptom = suggestions.get(position);
            editSearchSymptom.setText(clickedSymptom); // Fill the EditText with the selected suggestion
        });
    }

    private void updateSelectedSymptomsTextView() {
        StringBuilder displayText = new StringBuilder("Selected Symptoms:\n");
        for (String symptom : selectedSymptoms) {
            displayText.append("- ").append(symptom).append("\n");
        }
        selectedSymptomTextView.setText(displayText.toString().trim());
    }

    // SearchSymptom.java
    private void fetchSuggestions(String query) {
        suggestions.clear();
        suggestionAdapter.notifyDataSetChanged();

        String url = "http://192.168.0.16/BackEnd-APi/MedRec/api_search_symptom.php?query=" + query;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray responseArray = new JSONArray(response);
                        if (responseArray.length() > 0) {
                            for (int i = 0; i < responseArray.length(); i++) {
                                JSONObject symptomObject = responseArray.getJSONObject(i);
                                String symptomDescription = symptomObject.getString("description");
                                String symptomID = symptomObject.getString("id");

                                // Add only the description to the display list
                                suggestions.add(symptomDescription);

                                // Store the ID and Description in the map
                                selectedSymptomMap.put(symptomID, symptomDescription);
                            }
                            suggestionAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SearchSymptom.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(SearchSymptom.this, "Error fetching suggestions", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }
}
