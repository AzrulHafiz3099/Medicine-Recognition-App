package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class ForgotPasswordEmail extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_email);

        inputEmail = findViewById(R.id.email_input);
        btnContinue = findViewById(R.id.continue_button);

        btnContinue.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();

            // Validate email field
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordEmail.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(ForgotPasswordEmail.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email exists
            checkEmailInDatabase(email);
        });
    }

    private void checkEmailInDatabase(String email) {
        String url = getString(R.string.api_url); // API URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // Email exists, proceed to ForgotPasswordPins page
                                Intent intent = new Intent(ForgotPasswordEmail.this, ForgotPasswordPins.class);
                                intent.putExtra("email", email); // Pass email to the next page
                                startActivity(intent);
                            } else {
                                // Email does not exist
                                String message = jsonResponse.getString("message");
                                Toast.makeText(ForgotPasswordEmail.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ForgotPasswordEmail.this, "Response error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForgotPasswordEmail.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "checkEmailExist");
                params.put("email", email); // Send email to API
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
