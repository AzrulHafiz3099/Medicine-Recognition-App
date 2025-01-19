package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Search2 extends AppCompatActivity {

    private EditText searchQuery;
    private ListView suggestionsList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> suggestions = new ArrayList<>();
    private RequestQueue requestQueue;
    private TextView medicineName, medicineDetails, noResultTextView;
    private ImageView medicineImage;
    private View medicineDetailsCard;
    private ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // Initialize views
        searchQuery = findViewById(R.id.searchQuery);
        suggestionsList = findViewById(R.id.suggestionsList);
        medicineName = findViewById(R.id.medicineName);
        medicineDetails = findViewById(R.id.medicineDetails);
        noResultTextView = findViewById(R.id.noResultTextView);
        medicineImage = findViewById(R.id.medicineImage);
        medicineDetailsCard = findViewById(R.id.medicineDetailsCard);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Set up the adapter for ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestions);
        suggestionsList.setAdapter(adapter);

        // Inside your onCreate() or any other method where you need to update the UI
        String extractedText = getIntent().getStringExtra("extractedText");
        if (extractedText != null && !extractedText.isEmpty()) {
            // Using runOnUiThread to make sure UI updates happen on the main thread
            runOnUiThread(() -> {
                searchQuery.setText(extractedText);
                searchQuery.setSelection(extractedText.length()); // Set cursor at the end
            });
            fetchSuggestions(extractedText); // Automatically search when text is available
        } else {
            Toast.makeText(Search2.this, "No extracted text found", Toast.LENGTH_SHORT).show();
        }



        // Add TextWatcher to handle dynamic suggestion fetching
        searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    suggestions.clear();
                    adapter.notifyDataSetChanged();
                    medicineDetailsCard.setVisibility(View.GONE);
                    noResultTextView.setVisibility(View.GONE);

                    Glide.with(Search2.this).clear(medicineImage);
                    medicineImage.setVisibility(View.GONE);
                } else {
                    fetchSuggestions(query);
                    medicineImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        suggestionsList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMedicine = suggestions.get(position);
            String[] parts = selectedMedicine.split(" \\(ID: ");
            String drugId = parts[1].replace(")", "");

            Intent intent = new Intent(Search2.this, Suggestion.class);
            intent.putExtra("medicineName", selectedMedicine);
            intent.putExtra("drugId", drugId);
            startActivity(intent);

            suggestions.clear();
            adapter.notifyDataSetChanged();
        });

        imageBack = findViewById(R.id.img_back);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void fetchSuggestions(String query) {
        suggestions.clear();
        adapter.notifyDataSetChanged();

        // Split the query into individual keywords
        String[] queryArray = query.split(" "); // Splits by space, adjust if you need other delimiters

        // Build the query string by joining the array elements with commas or whatever separator you prefer
        String queryString = String.join(",", queryArray);

        String url = getString(R.string.api_suggestion2) + "?query=" + queryString; // Send the query string as a parameter
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray responseArray = new JSONArray(response);

                        if (responseArray.length() == 0) {
                            noResultTextView.setText("No Result Found");
                            noResultTextView.setVisibility(View.VISIBLE);
                            suggestionsList.setVisibility(View.GONE);
                        } else {
                            for (int i = 0; i < responseArray.length(); i++) {
                                JSONObject suggestion = responseArray.getJSONObject(i);
                                String drugId = suggestion.getString("DrugHeaderID");
                                String brandName = suggestion.getString("BrandName");
                                String genericName = suggestion.getString("GenericName");
                                String symptomDescription = suggestion.getString("SymptomDescription");

                                String suggestionText = brandName + " - " + genericName + " - " + symptomDescription + " (ID: " + drugId + ")";
                                suggestions.add(suggestionText);
                            }
                            adapter.notifyDataSetChanged();
                            noResultTextView.setVisibility(View.GONE);
                            suggestionsList.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Search2.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(Search2.this, "Error fetching suggestions", Toast.LENGTH_SHORT).show();
                    suggestionsList.setVisibility(View.GONE);
                });

        requestQueue.add(request);
    }

}