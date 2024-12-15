package com.workshop2.medrecog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private TextView profileName;
    private TextView profileEmail;
    private TextView profilePassword;
    private ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Initialize views
        profileName = findViewById(R.id.Fullname);
        profileEmail = findViewById(R.id.Email);
        profilePassword = findViewById(R.id.Password);
        profilePicture = findViewById(R.id.ProfilePicture);

        // Fetch and display the user profile
        getUserProfile();
    }

    private void getUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(R.string.api_url); // Replace with your actual API URL

        // Prepare the POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ProfileResponse", "Raw Response: " + response); // Log the raw response

                        if (response.startsWith("<")) { // Check if the response is an HTML error page
                            Toast.makeText(Profile.this, "Unexpected server response (HTML)", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if ("success".equals(status)) {
                                JSONObject data = jsonResponse.getJSONObject("data");
                                profileName.setText(data.getString("Fullname"));
                                profileEmail.setText(data.getString("Email"));
                                profilePassword.setText(data.getString("Password"));
                                // Load profile picture using Glide
                                String pictureUrl = data.getString("ProfilePicture");
                                if (!pictureUrl.isEmpty()) {
                                    Glide.with(Profile.this)
                                            .load(pictureUrl)
                                            .into(profilePicture);
                                } else {
                                    // Optionally set a default placeholder or handle empty profile picture case
                                    //profilePicture.setImageResource(R.drawable.default_profile_picture); // Replace with your placeholder image
                                }
                            } else {
                                String message = jsonResponse.getString("message");
                                Log.e("ProfileResponseError", "Error message: " + message);
                                Toast.makeText(Profile.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("ProfileResponseError", "JSON Parsing error", e);
                            Toast.makeText(Profile.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ProfileError", "Error occurred", error);
                        Toast.makeText(Profile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send user action and data to the PHP script
                Map<String, String> params = new HashMap<>();
                params.put("action", "getProfile");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Add JWT token in header
                return headers;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }
}
