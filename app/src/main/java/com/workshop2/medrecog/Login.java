package com.workshop2.medrecog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.workshop2.medrecog.databinding.LoginBinding;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private LoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set onClick listener for the Login button
        binding.loginButton.setOnClickListener(this::onLoginButtonClick);

        // Set onClick listener for the Forget Password button
        binding.forgetButton.setOnClickListener(this::onForgetButtonClick);  // Assuming you have a button with this ID in the XML

        // Check if the registration status message exists and show it
        String registrationStatus = getIntent().getStringExtra("REGISTRATION_STATUS");
        if (registrationStatus != null) {
            Toast.makeText(Login.this, registrationStatus, Toast.LENGTH_SHORT).show();
        }
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
        String url = getString(R.string.api_url);

        // Use Volley to make the API request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.trim().startsWith("{")) {
                                JSONObject jsonResponse = new JSONObject(response);
                                String status = jsonResponse.getString("status");
                                String message = jsonResponse.getString("message");

                                if ("success".equals(status)) {
                                    // Retrieve the token from the server response
                                    String token = jsonResponse.getString("token");

                                    // Save the token in SharedPreferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("jwt_token", token); // Replace old token with the new one
                                    editor.apply();

                                    Log.d("JWT_TOKEN", "Token saved successfully: " + token);

                                    // Navigate to another activity
                                    Intent intent = new Intent(Login.this, Homepage.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("API_ERROR", "Response is not valid JSON: " + response);
                                Toast.makeText(Login.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
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
                        String errorMessage = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMessage = "Error Code: " + error.networkResponse.statusCode;
                        } else if (error.getMessage() != null) {
                            errorMessage = error.getMessage();
                        }
                        Toast.makeText(Login.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "login");
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        // Add the request to the request queue
        Volley.newRequestQueue(Login.this).add(stringRequest);
    }

    // Function to handle Sign Up button click
    public void onSignupButtonClick(View view) {
        // Intent to navigate to the SignUpActivity (make sure the SignUpActivity is declared in AndroidManifest.xml)
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }

    // Function to handle Forget Password button click
    public void onForgetButtonClick(View view) {
        // Intent to navigate to the ForgetPass activity
        Intent intent = new Intent(Login.this, ForgetPass.class);
        startActivity(intent);
    }
}
