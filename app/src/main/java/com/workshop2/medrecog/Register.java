package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.workshop2.medrecog.databinding.RegisterBinding;

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

        // API URL to insert the user data
        String url = "http://192.168.0.15/MedRec/register_user.php"; // Change this URL to match your actual API location

        // Prepare the POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RegisterResponse", response);  // Log the response from the server

                        if (response.equalsIgnoreCase("New record created successfully")) {
                            // Registration successful, navigate to the next activity
                            Intent intent = new Intent(Register.this, Login.class);
                            intent.putExtra("REGISTRATION_STATUS", "Registration successful!");
                            startActivity(intent);
                            finish();
                        } else {
                            // Handle the failure response
                            Toast.makeText(Register.this, "Registration failed: " + response, Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Register.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                // Send user input to the PHP script
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("full_name", fullName);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(Register.this).add(stringRequest);
    }

    public void backLogin(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}
