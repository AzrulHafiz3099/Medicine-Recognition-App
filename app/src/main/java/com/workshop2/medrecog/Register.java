package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.workshop2.medrecog.databinding.RegisterBinding;

import org.json.JSONObject;

public class Register extends AppCompatActivity {

    private RegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = RegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set onClick listener for the Register button
        binding.registerButton.setOnClickListener(this::onRegisterButtonClick);
    }

    // Function to handle Register button click
    public void onRegisterButtonClick(View view) {
        String fullName = binding.fullName.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format using Android's built-in Patterns class
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Register.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // API URL for registering the user
        String url = getString(R.string.api_url); // Replace this with your API URL from strings.xml

        // Prepare the POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RegisterResponse", response);  // Log the raw response

                        try {
                            // Parse the API response
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                // Registration successful, retrieve UserID and proceed
                                String userID = jsonResponse.getString("UserID"); // Get UserID from the response
                                Log.d("UserID : ", userID);
                                createCart(userID); // Call addCart API after successful registration
                            } else {
                                // Show error message from the API response
                                Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Register.this, "Error parsing response register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Register.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("RegisterError", "Error occurred", error);
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                // Send user input and action to the PHP script
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("action", "register"); // Specify the action
                params.put("full_name", fullName);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(Register.this).add(stringRequest);
    }

    private void createCart(String userID) {
        String url = getString(R.string.api_cart); // Replace this with the addCart API URL

        StringRequest cartRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CartResponse", response);  // Log the full response for debugging

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            Log.d("Status : ", status);

                            if ("success".equals(status)) {
                                Toast.makeText(Register.this, "User successfully registered!", Toast.LENGTH_SHORT).show();
                                // Proceed to next screen
                                Intent intent = new Intent(Register.this, Login.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String message = jsonResponse.has("message") ? jsonResponse.getString("message") : "Unknown error";
                                Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Register.this, "Error parsing response cart", Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Register.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("CartError", "Error occurred", error);
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("action", "addCart"); // Specify the action for adding cart
                params.put("UserID", userID); // Pass the UserID received after registration
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(Register.this).add(cartRequest);
    }

}