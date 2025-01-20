package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class ForgotPasswordReset extends AppCompatActivity {

    private EditText password1Input, password2Input;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_reset);

        password1Input = findViewById(R.id.password1_input);
        password2Input = findViewById(R.id.password2_input);
        submitButton = findViewById(R.id.submit_button);

        // Set the OnClickListener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResetPasswordSubmit(view); // Call the method that handles the password reset
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do nothing, back button is disabled
    }





    // Call this method when the user clicks the submit button
    public void onResetPasswordSubmit(View view) {
        String password1 = password1Input.getText().toString().trim();
        String password2 = password2Input.getText().toString().trim();

        if (password1.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Please enter the password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password1.equals(password2)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming email was passed through intent from the previous activity
        String email = getIntent().getStringExtra("email");

        updatePassword(email, password1);
    }

    private void updatePassword(String email, String newPassword) {
        String url = getString(R.string.api_url); // API URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                Toast.makeText(ForgotPasswordReset.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ForgotPasswordReset.this, Login.class);
                                startActivity(intent);
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(ForgotPasswordReset.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ForgotPasswordReset.this, "Response error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForgotPasswordReset.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updatePassword");
                params.put("email", email);
                params.put("new_password", newPassword);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
