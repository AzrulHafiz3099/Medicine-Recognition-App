package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.workshop2.medrecog.databinding.LoginBinding;

import org.json.JSONArray;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private LoginBinding binding;
    private boolean isDatabaseChecked = false; // Flag to ensure toast is shown only once

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set onClick listener for the Login button
        binding.loginButton.setOnClickListener(this::onLoginButtonClick);

        // Check if the registration status message exists and show it
        String registrationStatus = getIntent().getStringExtra("REGISTRATION_STATUS");
        if (registrationStatus != null) {
            Toast.makeText(Login.this, registrationStatus, Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        // Show the database connection test toast only when the activity starts (not on resume)
//        if (!isDatabaseChecked) {
//            testDatabaseConnection();
//            isDatabaseChecked = true; // Set flag to true to prevent repeated toasts
//        }
//    }

    // Function to test database connection
    private void testDatabaseConnection() {
        String testUrl = "http://192.168.0.15/MedRec/fetch_users.php";

        // Use Volley to make a test API request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, testUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Login.this, "Database connected successfully!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Login.this, "Failed to connect to database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the test request to the Volley queue
        Volley.newRequestQueue(Login.this).add(stringRequest);
    }

    // Function to handle login button click
    public void onLoginButtonClick(View view) {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // API URL
        String url = "http://192.168.0.15/MedRec/fetch_users.php";

        // Use Volley to make the API request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the API response
                            JSONArray jsonResponse = new JSONArray(response);
                            boolean loginSuccess = false;

                            for (int i = 0; i < jsonResponse.length(); i++) {
                                JSONObject user = jsonResponse.getJSONObject(i);
                                String userEmail = user.getString("Email");
                                String userPassword = user.getString("Password");

                                // Check credentials
                                if (userEmail.equals(email) && userPassword.equals(password)) {
                                    loginSuccess = true;
                                    break;
                                }
                            }

                            if (loginSuccess) {
                                // Navigate to the next activity
                                Intent intent = new Intent(Login.this, com.workshop2.medrecog.Homepage.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the request queue
        Volley.newRequestQueue(Login.this).add(stringRequest);
    }

    // Function to handle Sign Up button click
    public void onSignupButtonClick(View view) {
        // Intent to navigate to the SignUpActivity (make sure the SignUpActivity is declared in AndroidManifest.xml)
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }
}
