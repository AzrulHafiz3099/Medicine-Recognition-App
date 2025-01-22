package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
    private ArrayList<String> selectedSymptomIds; // Store symptom IDs
    private RequestQueue requestQueue;
    private ImageView imageBack;

    private Map<String, String> selectedSymptomMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_symptom);

        editSearchSymptom = findViewById(R.id.edit_search_symptom);
        suggestionsList = findViewById(R.id.recyclerViewSuggestions);
        btnAddSymptom = findViewById(R.id.button_add_symptoms);
        btnSubmitSymptoms = findViewById(R.id.button_submit_symptoms);
        selectedSymptomTextView = findViewById(R.id.selected_symptom);

        suggestions = new ArrayList<>();
        selectedSymptoms = new ArrayList<>();
        selectedSymptomIds = new ArrayList<>();
        selectedSymptomMap = new HashMap<>();
        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestions);
        suggestionsList.setAdapter(suggestionAdapter);

        imageBack = findViewById(R.id.img_back);

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

            if (suggestions.contains(symptom) && !selectedSymptoms.contains(symptom)) {
                String symptomID = getSymptomIDByDescription(symptom);
                if (symptomID != null) {
                    selectedSymptoms.add(symptom);
                    selectedSymptomIds.add(symptomID); // Add symptom ID to the list
                    updateSelectedSymptomsTextView();
                    Toast.makeText(SearchSymptom.this, "Symptom added: " + symptom, Toast.LENGTH_SHORT).show();
                    editSearchSymptom.setText(""); // Clear input
                } else {
                    Toast.makeText(SearchSymptom.this, "Symptom not found in suggestions. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else if (!suggestions.contains(symptom)) {
                Toast.makeText(SearchSymptom.this, "Please select a symptom from the suggestions", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SearchSymptom.this, "Symptom already added or invalid", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle submitting symptoms
        btnSubmitSymptoms.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selectedSymptoms", selectedSymptoms);
            resultIntent.putStringArrayListExtra("selectedSymptomIds", selectedSymptomIds); // Pass IDs
            setResult(RESULT_OK, resultIntent);
            finish(); // Close the current activity and return to MainActivity
        });

        suggestionsList.setOnItemClickListener((parent, view, position, id) -> {
            String clickedSymptom = suggestions.get(position);
            editSearchSymptom.setText(clickedSymptom);
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

    private void updateSelectedSymptomsTextView() {
        StringBuilder displayText = new StringBuilder("Selected Symptoms:\n");
        for (String symptom : selectedSymptoms) {
            displayText.append("- ").append(symptom).append("\n");
        }
        selectedSymptomTextView.setText(displayText.toString().trim());
    }

    private void fetchSuggestions(String query) {
        suggestions.clear();

        String url = "https://www.etourmersing.com/BackEnd-APi/MedRec/api_search_symptom.php?query=" + query;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray responseArray = new JSONArray(response);
                        if (responseArray.length() > 0) {
                            for (int i = 0; i < responseArray.length(); i++) {
                                JSONObject symptomObject = responseArray.getJSONObject(i);
                                String symptomDescription = symptomObject.getString("description");
                                String symptomID = symptomObject.getString("id");

                                if (!suggestions.contains(symptomDescription)) {
                                    suggestions.add(symptomDescription);
                                    selectedSymptomMap.put(symptomID, symptomDescription); // Keep mapping
                                }
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

    private String getSymptomIDByDescription(String description) {
        for (Map.Entry<String, String> entry : selectedSymptomMap.entrySet()) {
            if (entry.getValue().trim().equalsIgnoreCase(description.trim())) {
                return entry.getKey(); // Return the ID
            }
        }
        return null;
    }
}
