package com.workshop2.medrecog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Receipt extends AppCompatActivity {

    private String cartID, paymentMethod, billCode, cartIDFPX;
    private TextView txtOrderId, txtVendor, txtGenericName, txtQuantity, txtPrice, txtTotal, txtPaymentMethod;

    private ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt);

        txtOrderId = findViewById(R.id.orderId);
        txtVendor = findViewById(R.id.vendor);
        txtGenericName = findViewById(R.id.GenericName);
        txtQuantity = findViewById(R.id.quantity);
        txtPrice = findViewById(R.id.price);
        txtTotal = findViewById(R.id.totalPrice);
        txtPaymentMethod = findViewById(R.id.paymentMethod);

        imageBack = findViewById(R.id.img_back);

        // Get data from the intent
        cartID = getIntent().getStringExtra("CART_ID");

        paymentMethod = getIntent().getStringExtra("PAYMENT_METHOD");

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        cartIDFPX = sharedPreferences.getString("cartIDFPX", null);
        billCode = sharedPreferences.getString("billCode", null);

        Log.d("SharedPrefs", "CartIDFPX: " + cartIDFPX + ", BillCode: " + billCode);

        // Call appropriate method based on payment method
        if ("COD".equals(paymentMethod)) {
            Log.d("Receipt", "CART_ID: " + cartID);
            addReceipt(); // Call the receipt method for COD
        } else {
            addReceiptFPX(); // Pass BillCode to the FPX receipt
            Log.d("BillCode", "BILLCODE :" + billCode);
            Log.d("CartID", "CARTID :" + cartIDFPX);
        }

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

        handleIncomingIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update intent
        handleIncomingIntent(intent); // Handle new intent
    }

    private void handleIncomingIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.getScheme().equals("yourapp") && data.getHost().equals("payment-complete")) {
            // Process the payment result (e.g., confirm the payment, show receipt)
            String paymentStatus = data.getQueryParameter("status"); // Retrieve the payment status
            Log.d("FPX PAYMENT STATUS", "FPX PAYMENT STATUS: " + paymentStatus);
            //String cartID = data.getQueryParameter("cartID"); // Retrieve the cartID
            //String billCode = data.getQueryParameter("BillCode");

            Log.d("SharedPrefs", "CartIDFPX: " + cartIDFPX + ", BillCode: " + billCode);

            // Log or use the CartID for your needs
            if (cartID != null) {
                Log.d("Payment", "CartIDFPX: " + cartIDFPX); // Log the CartID for debugging
                Log.d("Payment", "BillCode: " + billCode);
                // You can now use the cartID, for example, load the receipt or display relevant data
            }
        }
    }


    private void addReceipt(){
        String url = getString(R.string.api_receipt);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the response as an object first
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                // Since the receipt is an array, use getJSONArray instead of getJSONObject
                                JSONArray receiptArray = jsonResponse.getJSONArray("receipt");

                                if (receiptArray.length() > 0) {
                                    // Use the first element of the array if you expect only one receipt
                                    JSONObject receipt = receiptArray.getJSONObject(0);

                                    // Extract the receipt details from the response
                                    String orderID = receipt.getString("OrderID");
                                    String genericNames = receipt.getString("GenericNames");
                                    String quantities = receipt.getString("Quantities");
                                    String paymentDate = receipt.getString("PaymentDate");
                                    String totalAmount = receipt.getString("TotalAmount");
                                    String paymentMethod = receipt.getString("PaymentMethod");
                                    String vendor = receipt.getString("Vendor");

                                    // Set the UI elements with the received data
                                    txtOrderId.setText(orderID);
                                    txtVendor.setText(vendor);
                                    txtGenericName.setText(genericNames);
                                    txtQuantity.setText(quantities);
                                    txtPrice.setText(totalAmount);
                                    txtTotal.setText(totalAmount);
                                    txtPaymentMethod.setText(paymentMethod);

                                } else {
                                    // Handle failure when the array is empty
                                    Toast.makeText(Receipt.this, "No receipt found", Toast.LENGTH_SHORT).show();
                                }

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

    private void addReceiptFPX(){
        Toast.makeText(Receipt.this, "THIS IS FPX", Toast.LENGTH_SHORT).show();
        Log.d("DATA IN ADD RECEIPT FPX", "DATA IN ADD RECEIPT FPX"+"CartIDFPX: " + cartIDFPX + ", BillCode: " + billCode);

        String url = getString(R.string.api_receipt);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the response as an object first
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                // Since the receipt is an array, use getJSONArray instead of getJSONObject
                                JSONArray receiptArray = jsonResponse.getJSONArray("receipt");

                                if (receiptArray.length() > 0) {
                                    // Use the first element of the array if you expect only one receipt
                                    JSONObject receipt = receiptArray.getJSONObject(0);

                                    // Extract the receipt details from the response
                                    String orderID = receipt.getString("OrderID");
                                    String genericNames = receipt.getString("GenericNames");
                                    String quantities = receipt.getString("Quantities");
                                    String paymentDate = receipt.getString("PaymentDate");
                                    String totalAmount = receipt.getString("TotalAmount");
                                    String paymentMethod = receipt.getString("PaymentMethod");
                                    String vendor = receipt.getString("Vendor");

                                    // Set the UI elements with the received data
                                    txtOrderId.setText(orderID);
                                    txtVendor.setText(vendor);
                                    txtGenericName.setText(genericNames);
                                    txtQuantity.setText(quantities);
                                    txtPrice.setText(totalAmount);
                                    txtTotal.setText(totalAmount);
                                    txtPaymentMethod.setText(paymentMethod);

                                } else {
                                    // Handle failure when the array is empty
                                    Toast.makeText(Receipt.this, "No receipt found", Toast.LENGTH_SHORT).show();
                                }

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
                params.put("action", "addReceiptFPX");
                params.put("cartIDFPX", cartIDFPX);
                params.put("billCode", billCode);
                return params;
            }
        };

        // Add the request to the request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

}