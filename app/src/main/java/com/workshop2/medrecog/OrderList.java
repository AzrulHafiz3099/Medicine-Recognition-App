package com.workshop2.medrecog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.workshop2.medrecog.adapter.TrackingAdapter;
import com.workshop2.medrecog.adapter.TrackingItem;
import com.workshop2.medrecog.adapter.TrackingStep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class OrderList extends AppCompatActivity {

    private RecyclerView trackingRecyclerView;
    private String userID;
    private ImageView imageBack;
    private TrackingAdapter trackingAdapter;
    private List<TrackingItem> trackingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list);

        imageBack = findViewById(R.id.img_back); // Find the imageIcon

        // Handle back button behavior with the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close the activity
            }
        });

        imageBack.setOnClickListener(v -> onBackPressed());

        // Get userID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        userID = sharedPreferences.getString("UserID", "");

        trackingRecyclerView = findViewById(R.id.tracking_recycler_view);
        trackingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        trackingItems = new ArrayList<>();
        trackingAdapter = new TrackingAdapter(trackingItems);
        trackingRecyclerView.setAdapter(trackingAdapter);

        // Fetch orders from the API
        if (!userID.isEmpty()) {
            fetchOrders(userID);
        } else {
            Toast.makeText(this, "UserID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrders(String userID) {
        String url = getString(R.string.api_order); // Replace with your actual API URL for fetching orders

        // Prepare the Volley request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("OrderListResponse", "Raw Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONArray orders = jsonResponse.getJSONArray("orders");
                                    trackingItems.clear(); // Clear existing order data

                                for (int i = 0; i < orders.length(); i++) {
                                    JSONObject order = orders.getJSONObject(i);

                                    String orderID = order.getString("OrderID");
                                    String cartID = order.getString("CartID");
                                    double totalPrice = order.getDouble("TotalPrice");
                                    String orderDate = order.getString("OrderDate");
                                    String orderPaymentMethod = order.getString("OrderPaymentMethod");
                                    String paymentStatus = order.getString("PaymentStatus");

                                    // Handle quantities as an array of strings
                                    JSONArray quantitiesArray = order.getJSONArray("Quantities");
                                    ArrayList<Integer> quantities = new ArrayList<>();
                                    for (int j = 0; j < quantitiesArray.length(); j++) {
                                        quantities.add(Integer.parseInt(quantitiesArray.getString(j)));
                                    }

                                    // Handle generic names as an array of strings
                                    JSONArray genericNamesArray = order.getJSONArray("GenericNames");
                                    ArrayList<String> genericNames = new ArrayList<>();
                                    for (int j = 0; j < genericNamesArray.length(); j++) {
                                        genericNames.add(genericNamesArray.getString(j));
                                    }

                                    // Continue parsing other fields as needed
                                    String receiptID = order.getString("ReceiptID");
                                    String userID = order.getString("UserID");
                                    String paymentDate = order.getString("PaymentDate");  // Fetch paymentDate here
                                    double totalAmount = order.getDouble("TotalAmount");
                                    String receiptPaymentMethod = order.getString("ReceiptPaymentMethod");
                                    String transactionReference = order.getString("TransactionReference");
                                    String vendor = order.getString("Vendor");

                                    // Use the quantities array for creating tracking items
                                    int totalItems = quantities.stream().mapToInt(Integer::intValue).sum(); // Sum up all quantities

                                    // Create tracking steps dynamically
                                    List<TrackingStep> trackingSteps = createTrackingSteps(paymentDate, paymentStatus);

                                    // Add the order to the trackingItems list
                                    // Add the payment status when creating a TrackingItem
                                    trackingItems.add(createTrackingItem(orderID, orderDate, totalItems, String.valueOf(totalAmount), trackingSteps, orderPaymentMethod, paymentStatus));


                                }



                                // Notify the adapter that data has changed
                                trackingAdapter.notifyDataSetChanged();
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(OrderList.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("OrderListResponseError", "JSON Parsing error", e);
                            Toast.makeText(OrderList.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("OrderListError", "Error occurred", error);
                        Toast.makeText(OrderList.this, "Error fetching order data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getOrdersByUserID");  // Action for fetching orders
                params.put("userID", userID);  // Pass the userID to fetch specific orders
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private List<TrackingStep> createTrackingSteps(String paymentDate, String paymentStatus) {
        List<TrackingStep> steps = new ArrayList<>();

        // Add "Placed" step using the actual payment date
        steps.add(new TrackingStep("Placed", paymentDate, true));

        // Only add "Picked Up" step if payment status is not "Failed"
        if (!"Failed".equals(paymentStatus)) {
            steps.add(new TrackingStep("Picked Up", "pending", false));
        }

        return steps;
    }


    private TrackingItem createTrackingItem(String orderNumber, String placedDate, int itemCount, String itemTotal, List<TrackingStep> steps, String paymentMethod, String paymentStatus) {
        return new TrackingItem(orderNumber, placedDate, itemCount, itemTotal, steps, paymentMethod, paymentStatus);
    }

}
