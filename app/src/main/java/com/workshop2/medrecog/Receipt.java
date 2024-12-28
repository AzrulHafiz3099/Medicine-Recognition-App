package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Receipt extends AppCompatActivity {

    private String cartID;
    private TextView txtOrderId, txtVendor, txtGenericName, txtQuantity, txtPrice, txtTotal, txtPaymentMethod;

    private ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.receipt);

        txtOrderId = findViewById(R.id.orderId);
        txtVendor = findViewById(R.id.vendor);
        txtGenericName = findViewById(R.id.GenericName);
        txtQuantity = findViewById(R.id.quantity);
        txtPrice = findViewById(R.id.price);
        txtTotal = findViewById(R.id.totalPrice);
        txtPaymentMethod = findViewById(R.id.paymentMethod);

        imageBack = findViewById(R.id.img_back);

        cartID = getIntent().getStringExtra("CART_ID");
        Log.d("Receipt", "CART_ID: " + cartID);

        addReceipt();

        // Set click listener on the image_icon
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to Homepage activity
                Intent intent = new Intent(Receipt.this, Homepage.class);
                startActivity(intent);
                finish(); // Optional: To close the ProductDesc activity if needed
            }
        });
    }

    private void addReceipt(){
        String url = getString(R.string.api_receipt);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                JSONObject receipt = jsonResponse.getJSONObject("receipt");

                                // Extract the receipt details from the response
                                String orderID = receipt.getString("OrderID");
                                String genericNames = receipt.getString("GenericNames");
                                String quantities = receipt.getString("Quantities");
                                String paymentDate = receipt.getString("PaymentDate");
                                String totalAmount = receipt.getString("TotalAmount");
                                String paymentMethod = receipt.getString("PaymentMethod");
                                String vendor = receipt.getString("Vendor");

                                txtOrderId.setText(orderID);
                                txtVendor.setText(vendor);
                                txtGenericName.setText(genericNames);
                                txtQuantity.setText(quantities);
                                txtPrice.setText(totalAmount);
                                txtTotal.setText(totalAmount);
                                txtPaymentMethod.setText(paymentMethod);

                            } else {
                                // Handle failure
                                Toast.makeText(Receipt.this, "Failed to add receipt: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("CartUpdateError", "JSON Parsing error", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AddReceiptError", "Volley error", error);
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "addReceipt");
                params.put("cartID", cartID);
                return params;
            }
        };

        // Add the request to the request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

}