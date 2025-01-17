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

        // On item click, fetch details for the selected suggestion
        suggestionsList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMedicine = suggestions.get(position);

            // Start the SuggestionActivity and pass the medicine name
            Intent intent = new Intent(Search.this, Suggestion.class);
            intent.putExtra("medicineName", selectedMedicine);
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
    private void fetchSuggestions(String query) {
        // Clear details when a new search is initiated
        medicineDetails.setText(""); // Clear the details text

        // Set up the URL for the API request with the query parameter
        String url = getString(R.string.api_suggestion) + query;
        //String url = "http://192.168.0.16/BackEnd-APi/MedRec/api_suggestion.php?query=" + query;

        // Create a new JsonArrayRequest to fetch suggestions from the API
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        suggestions.clear(); // Clear previous suggestions

                        // Check if the response is empty
                        if (response.length() == 0) {
                            noResultTextView.setText("No Result Found");
                            noResultTextView.setVisibility(View.VISIBLE);
                            suggestionsList.setVisibility(View.GONE); // Hide the ListView
                        } else {
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    suggestions.add(response.getString(i)); // Add each suggestion to the list
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Notify the adapter that the data has changed, so the ListView can update
                            adapter.notifyDataSetChanged();
                            noResultTextView.setVisibility(View.GONE); // Hide "No Result Found" message
                            suggestionsList.setVisibility(View.VISIBLE); // Show the ListView
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log error and show a toast message for error handling
                        Log.e("API Error", "Error fetching suggestions", error);
                        Toast.makeText(Search.this, "Error fetching suggestions", Toast.LENGTH_SHORT).show();

                        // Hide the ListView in case of an error
                        suggestionsList.setVisibility(View.GONE);
                    }
                });

        // Add the request to the request queue for execution
        requestQueue.add(request);
    }

    // Fetch and show details of the selected medicine
    // Inside the fetchMedicineDetails method
    private void fetchMedicineDetails(String medicineName) {
        String url = getString(R.string.api_search) + medicineName;
        //String url = "http://192.168.0.16/BackEnd-APi/MedRec/api_search.php?name=" + medicineName;

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
