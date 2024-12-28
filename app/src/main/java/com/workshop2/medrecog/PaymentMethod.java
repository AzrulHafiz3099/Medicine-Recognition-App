package com.workshop2.medrecog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethod extends AppCompatActivity {

    private String totalPrice;
    private String userID;
    private String cartID;
    private ImageView imageBack;
    private FrameLayout paymentCODButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_method);

        imageBack = findViewById(R.id.img_back);

        // Get the data passed from the CartProduct activity
        Intent intent = getIntent();
        totalPrice = intent.getStringExtra("TOTAL_PRICE");
        userID = intent.getStringExtra("USER_ID");
        cartID = intent.getStringExtra("CART_ID");

        // Log values for verification
        Log.d("PaymentMethod", "CartID: " + cartID);
        Log.d("PaymentMethod", "UserID: " + userID);
        Log.d("PaymentMethod", "Total Price: " + totalPrice);

        paymentCODButton = findViewById(R.id.payment_COD);  // COD button reference


        // Handle COD selection
        paymentCODButton.setOnClickListener(v -> {
            // Call the method to process the COD payment
            processCODPayment();
        });

        // Set click listener on the image_icon
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to Homepage activity
                Intent intent = new Intent(PaymentMethod.this, Cart.class);
                startActivity(intent);
                finish(); // Optional: To close the ProductDesc activity if needed
            }
        });
    }

    private void processCODPayment() {
        String url = getString(R.string.api_order); // API URL for order creation

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                // Order placed successfully
                                Toast.makeText(PaymentMethod.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                updateCartStatus();
//                                Intent intent = new Intent(PaymentMethod.this, PaymentMethod.class);
//                                startActivity(intent);  // Start the PaymentMethod activity
                                // Navigate to order confirmation screen (or another screen)
                                //finish(); // Close the payment screen
                            } else {
                                // Handle failure
                                Toast.makeText(PaymentMethod.this, "Failed to place the order: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("OrderError", "JSON Parsing error", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("OrderError", "Volley error", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_order");
                params.put("userID", userID);
                params.put("cartID", cartID);
                params.put("totalPrice", totalPrice);
                params.put("paymentMethod", "COD"); // Payment method is COD
                return params;
            }
        };

        // Add the request to the request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    // Method to update the cart status to "completed"
    private void updateCartStatus() {
        String url = getString(R.string.api_cart); // API URL for updating cart status

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                //testing
                                Intent intent = new Intent(PaymentMethod.this, Receipt.class);
                                intent.putExtra("CART_ID", cartID);
                                startActivity(intent);
                                // Cart status updated successfully
                                //Toast.makeText(PaymentMethod.this, "Cart status updated to completed", Toast.LENGTH_SHORT).show();
                                // Navigate to the next screen (e.g., order confirmation)
                                //finish();
                            } else {
                                // Handle failure
                                Toast.makeText(PaymentMethod.this, "Failed to update cart: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("CartUpdateError", "JSON Parsing error", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CartUpdateError", "Volley error", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateCart");
                params.put("userID", userID);
                params.put("cartID", cartID);
                return params;
            }
        };

        // Add the request to the request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
