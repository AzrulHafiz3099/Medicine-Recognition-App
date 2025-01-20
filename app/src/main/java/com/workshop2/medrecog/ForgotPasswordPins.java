package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordPins extends AppCompatActivity {

    private String email;
    private EditText inputPins;
    private String sentPin;  // Variable to store the sent PIN
    private Button verifyButton; // Add a button to verify the PIN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_pins);

        // Retrieve email passed from the previous activity
        email = getIntent().getStringExtra("email");
        inputPins = findViewById(R.id.pins_input);
        verifyButton = findViewById(R.id.verify_button); // Button for verification

        // Send 6-digit PIN to the email
        sendEmailWithPin(email);

        // Set up the button listener for verification
        verifyButton.setOnClickListener(v -> {
            String enteredPin = inputPins.getText().toString().trim();
            if (enteredPin.isEmpty()) {
                Toast.makeText(ForgotPasswordPins.this, "Please enter the PIN", Toast.LENGTH_SHORT).show();
            } else if (enteredPin.equals(sentPin)) {
                // If PIN matches, navigate to ForgotPasswordReset activity
                Intent intent = new Intent(ForgotPasswordPins.this, ForgotPasswordReset.class);
                intent.putExtra("email", email);  // Pass the email to the next activity
                startActivity(intent);
            } else {
                // If PIN doesn't match, show an error
                Toast.makeText(ForgotPasswordPins.this, "Incorrect PIN. Please try again.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotPasswordPins.this, Login.class);
                startActivity(intent);
            }
        });
    }

    private void sendEmailWithPin(String email) {
        String url = getString(R.string.api_send_email); // Update with your PHP script URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // PIN sent successfully
                                sentPin = jsonResponse.getString("pin");  // Store the sent PIN
                                Log.d("PIN", "PIN sent: " + sentPin);
                                Toast.makeText(ForgotPasswordPins.this, "PIN sent to " + email, Toast.LENGTH_SHORT).show();

                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(ForgotPasswordPins.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ForgotPasswordPins.this, "Response error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForgotPasswordPins.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email); // Send email to the server
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }
}
