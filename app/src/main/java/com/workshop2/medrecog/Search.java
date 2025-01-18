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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import android.util.Log;  // Import Log for debugging

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Search extends AppCompatActivity {

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

        // Get the extracted text passed from the MedicineReco activity
        String extractedText = getIntent().getStringExtra("extractedText");

        // Set the extracted text into the search query EditText
        if (extractedText != null && !extractedText.isEmpty()) {
            searchQuery.setText(extractedText); // Set the text to the EditText
            fetchSuggestions(extractedText); // Trigger the search based on the extracted text
        }

        // Set up the adapter for ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestions);
        suggestionsList.setAdapter(adapter);

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
                    suggestions.clear(); // Clear suggestions
                    adapter.notifyDataSetChanged();
                    medicineDetailsCard.setVisibility(View.GONE);  // Hide medicine details view
                    noResultTextView.setVisibility(View.GONE); // Hide "No Result Found" text

                    // Clear the image and hide the ImageView
                    Glide.with(Search.this).clear(medicineImage);
                    medicineImage.setVisibility(View.GONE);
                } else {
                    fetchSuggestions(query);  // Fetch suggestions when user types
                    medicineImage.setVisibility(View.VISIBLE); // Show the ImageView when there’s input
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        suggestionsList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMedicine = suggestions.get(position);

            // Extract the ID from the selected item
            String[] parts = selectedMedicine.split(" \\(ID: ");
            String drugId = parts[1].replace(")", ""); // Extracting ID

            // Start the SuggestionActivity and pass the medicine name and drug ID
            Intent intent = new Intent(Search.this, Suggestion.class);
            intent.putExtra("medicineName", selectedMedicine);
            intent.putExtra("drugId", drugId);  // Pass the drug ID
            startActivity(intent);

            // Clear the suggestions list
            suggestions.clear();
            adapter.notifyDataSetChanged();
        });


        imageBack = findViewById(R.id.img_back); // Find the imageIcon

        // Handle back button behavior with the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close the activity
            }
        });

        imageBack.setOnClickListener(v -> onBackPressed());
    }

    // Fetch suggestions from the backend based on query
    // Fetch suggestions from the backend based on query
    private void fetchSuggestions(String query) {
        // Clear previous suggestions
        suggestions.clear();
        adapter.notifyDataSetChanged();

        // Set up the URL for the API request with the query parameter
        String url = getString(R.string.api_suggestion) + query;

        // Create a new StringRequest to fetch suggestions from the API
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("API Response", "Response: " + response);

                        try {
                            // Parse the response as a JSONArray
                            JSONArray responseArray = new JSONArray(response);

                            // Check if the response is empty
                            if (responseArray.length() == 0) {
                                noResultTextView.setText("No Result Found");
                                noResultTextView.setVisibility(View.VISIBLE);
                                suggestionsList.setVisibility(View.GONE); // Hide the ListView
                            } else {
                                for (int i = 0; i < responseArray.length(); i++) {
                                    JSONObject suggestion = responseArray.getJSONObject(i);

                                    // Fetch the drug ID and other details
                                    String drugId = suggestion.getString("DrugHeaderID");
                                    String brandName = suggestion.getString("BrandName");
                                    String genericName = suggestion.getString("GenericName");
                                    String symptomDescription = suggestion.getString("SymptomDescription");

                                    // Format the suggestion text to include the ID
                                    String suggestionText = brandName + " - " + genericName + " - " +
                                            symptomDescription + " (ID: " + drugId + ")";

                                    // Add each suggestion to the list
                                    suggestions.add(suggestionText);
                                }

                                // Notify the adapter that the data has changed
                                adapter.notifyDataSetChanged();
                                noResultTextView.setVisibility(View.GONE); // Hide "No Result Found" message
                                suggestionsList.setVisibility(View.VISIBLE); // Show the ListView
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Search.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API Error", "Error fetching suggestions", error);
                        Toast.makeText(Search.this, "Error fetching suggestions", Toast.LENGTH_SHORT).show();
                        suggestionsList.setVisibility(View.GONE);
                    }
                });

        // Add the request to the request queue for execution
        requestQueue.add(request);
    }




    // Fetch and show details of the selected medicine
    private void fetchMedicineDetails(String medicineName) {
        String url = getString(R.string.api_search) + medicineName;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Use local variable directly for text setting
                            TextView medicineNameTextView = findViewById(R.id.medicineName); // Local TextView
                            String name = response.getString("BrandName");
                            medicineNameTextView.setText(name);  // Use local TextView

                            String details = "Generic Name: " + response.getString("GenericName") + "\n" +
                                    "Dosage: " + response.getString("Dosage") + "\n" +
                                    "Manufacturer: " + response.getString("Manufacturer") + "\n" +
                                    "Side Effects: " + response.getString("SideEffects");
                            TextView medicineDetailsTextView = findViewById(R.id.medicineDetails);  // Local TextView
                            medicineDetailsTextView.setText(details);  // Use local TextView

                            // Load the image with Glide
                            ImageView medicineImage = findViewById(R.id.medicineImage); // Local ImageView
                            Glide.with(Search.this)
                                    .load("http://192.168.0.16/BackEnd-APi/MedRec/DrugImage/" + response.getString("DrugImage"))
                                    .into(medicineImage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            TextView medicineDetailsTextView = findViewById(R.id.medicineDetails);
                            medicineDetailsTextView.setText("Error fetching details");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        TextView medicineDetailsTextView = findViewById(R.id.medicineDetails);
                        medicineDetailsTextView.setText("Error fetching details");
                    }
                });

        requestQueue.add(request);
    }
}
