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

import java.util.HashMap;
import java.util.Map;

public class Receipt extends AppCompatActivity {

    private String cartID, paymentMethod, billCode, cartIDFPX, userID;
    private TextView txtOrderId, txtVendor, txtGenericName, txtQuantity, txtPrice, txtTotal, txtPaymentMethod;
    private ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt);

        // Initialize UI elements
        txtOrderId = findViewById(R.id.orderId);
        txtVendor = findViewById(R.id.vendor);
        txtGenericName = findViewById(R.id.GenericName);
        txtQuantity = findViewById(R.id.quantity);
        txtPrice = findViewById(R.id.price);
        txtTotal = findViewById(R.id.totalPrice);
        txtPaymentMethod = findViewById(R.id.paymentMethod);
        imageBack = findViewById(R.id.img_back);

        // Retrieve intent data
        cartID = getIntent().getStringExtra("CART_ID");
        paymentMethod = getIntent().getStringExtra("PAYMENT_METHOD");

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        cartIDFPX = sharedPreferences.getString("cartIDFPX", null);
        billCode = sharedPreferences.getString("billCode", null);

        SharedPreferences sharedPreferences2 = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        userID = sharedPreferences2.getString("UserID", null);

        Log.d("SharedPrefs", "CartIDFPX: " + cartIDFPX + ", BillCode: " + billCode + "USER : " + userID);

        // Call appropriate receipt method based on payment method
        if ("COD".equals(paymentMethod)) {
            addReceipt(); // Call COD-specific receipt method
        }

        // Handle incoming FPX intent data
        handleIncomingIntent(getIntent());

        // Back button listener
        imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(Receipt.this, Homepage.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            Log.d("PAYMENT URI", "Received URI: " + data.toString());

            if (data.getScheme().equals("yourapp") && data.getHost().equals("payment-complete")) {
                String statusId = data.getQueryParameter("status_id");
                String cartID = data.getQueryParameter("cartID");

                Log.d("PAYMENT STATUS", "Extracted status_id: " + statusId);
                Log.d("PAYMENT CARTID", "Extracted cartID: " + cartID);

                if (statusId != null) {
                    switch (statusId) {
                        case "1": // success
                            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
                            addReceiptFPX();
                            updatePaymentStatus(cartID, "1");  // Update status to 'success'
                            break;
                        case "2": // pending
                            Toast.makeText(this, "Payment Pending. Please wait.", Toast.LENGTH_SHORT).show();
                            break;
                        case "3": // failed
                            updateCartStatusFailed(cartID);
                            Toast.makeText(this, "Payment Failed. Please try again.", Toast.LENGTH_SHORT).show();
                            updatePaymentStatus(cartID, "3");  // Update status to 'failed'
                            Intent intent2 = new Intent(Receipt.this, Homepage.class);
                            startActivity(intent2);
                            break;
                        default:
                            Log.e("PAYMENT ERROR", "Unknown status_id: " + statusId);
                            break;
                    }
                } else {
                    Log.e("PAYMENT ERROR", "Status ID is null. Check the URI structure.");
                }
            } else {
                Log.e("PAYMENT ERROR", "Unexpected URI: " + data.toString());
            }
        } else {
            Log.e("PAYMENT ERROR", "No data found in the intent.");
        }
    }


    private void addReceipt() {
        String url = getString(R.string.api_receipt);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            JSONArray receiptArray = jsonResponse.getJSONArray("receipt");

                            if (receiptArray.length() > 0) {
                                JSONObject receipt = receiptArray.getJSONObject(0);
                                updateReceiptUI(receipt);
                                updateDrugQuantity(cartID);
                            } else {
                                Toast.makeText(this, "No receipt found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "(COD) Failed to add receipt: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("AddReceiptError", "JSON Parsing error", e);
                    }
                },
                error -> Log.e("AddReceiptError", "Volley error", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "addReceipt");
                params.put("cartID", cartID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void addReceiptFPX() {
        String url = getString(R.string.api_receipt);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            updateCartStatus();
                            JSONArray receiptArray = jsonResponse.getJSONArray("receipt");

                            if (receiptArray.length() > 0) {
                                JSONObject receipt = receiptArray.getJSONObject(0);
                                updateReceiptUI(receipt);
                                updateDrugQuantity(cartIDFPX);
                            } else {
                                Toast.makeText(this, "No receipt found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "(FPX) Failed to add receipt: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("AddReceiptFPXError", "JSON Parsing error", e);
                    }
                },
                error -> Log.e("AddReceiptFPXError", "Volley error", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "addReceiptFPX");
                params.put("cartIDFPX", cartIDFPX);
                params.put("billCode", billCode);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void updateCartStatus() {
        String url = getString(R.string.api_cart);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Cart updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to update cart: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("CartUpdateError", "JSON Parsing error", e);
                    }
                },
                error -> Log.e("CartUpdateError", "Volley error", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateCart");
                params.put("cartID", cartIDFPX);
                params.put("userID", userID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void updateCartStatusFailed(String cartID) {
        String url = getString(R.string.api_cart);

        Log.d("UPDATE CART FAILED", cartID);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Cart updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to update cart: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("CartUpdateError", "JSON Parsing error", e);
                    }
                },
                error -> Log.e("CartUpdateError", "Volley error", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateCartFailed");
                params.put("cartID", cartID);
                params.put("userID", userID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void updatePaymentStatus(final String cartID, final String statusId) {
        String url = getString(R.string.api_order);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Log.d("PaymentUpdate", "Payment status updated successfully.");
                        } else {
                            Log.e("PaymentUpdate", "Failed to update payment status: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e("PaymentUpdateError", "JSON Parsing error", e);
                    }
                },
                error -> Log.e("PaymentUpdateError", "Volley error", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updatePaymentStatus");
                params.put("cartID", cartID);
                params.put("statusId", statusId);  // statusId: '1' for success, '3' for failed
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void updateDrugQuantity(String cartID) {
        String url = getString(R.string.api_drug_supply);

        // Request to get drug quantities from cart
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DrugQuantityUpdate", "Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        // Log the cartID received in the response
                        String cartIDT = jsonResponse.getString("cartID");
                        Log.d("DrugQuantityUpdate", "Received CartID: " + cartIDT);

                        if ("success".equals(status)) {
                            //Toast.makeText(this, "Drug quantities updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to update drug quantities", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("DrugQuantityUpdate", "JSON Parsing error", e);
                    }
                },
                error -> Log.e("DrugQuantityUpdate", "Volley error", error)) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateDrugQuantity");
                params.put("cartID", cartID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }



    private void updateReceiptUI(JSONObject receipt) throws JSONException {
        txtOrderId.setText(receipt.getString("OrderID"));
        txtVendor.setText(receipt.getString("Vendor"));
        txtGenericName.setText(receipt.getString("GenericNames"));
        txtQuantity.setText(receipt.getString("Quantities"));
        txtPrice.setText(receipt.getString("TotalAmount"));
        txtTotal.setText(receipt.getString("TotalAmount"));
        txtPaymentMethod.setText(receipt.getString("PaymentMethod"));
    }
}