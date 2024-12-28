package com.workshop2.medrecog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.text.DecimalFormat;

public class CartProduct extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<Cart> cartList;
    private TextView txtZero, textSubTotal;
    private ImageView imageBack;
    private Button btnPay;
    private LinearLayout emptyCartLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cartproduct);

        // Retrieve userID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String savedUserID = sharedPreferences.getString("UserID", "");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView    .setLayoutManager(new LinearLayoutManager(this));
        emptyCartLayout = findViewById(R.id.container_is_empty);

        txtZero = findViewById(R.id.txt_zero);
        textSubTotal = findViewById(R.id.text_sub_total_value);
        imageBack = findViewById(R.id.img_back); // Find the imageIcon
        btnPay = findViewById(R.id.button_pay);

        // Initialize cart list and adapter
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList);
        recyclerView.setAdapter(cartAdapter);

        if (!savedUserID.isEmpty()) {
            fetchCartItems(savedUserID);
        } else {
            Toast.makeText(this, "UserID not found. Please log in.", Toast.LENGTH_SHORT).show();
        }

        // Set click listener on the image_icon
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to Homepage activity
                Intent intent = new Intent(CartProduct.this, Homepage.class);
                startActivity(intent);
                finish(); // Optional: To close the ProductDesc activity if needed
            }
        });

        String totalPrice = textSubTotal.getText().toString();  // Get the formatted total price

        // Check if totalPrice is "0" or "0.00" or "RM 0" or "RM 0.00"
        if (totalPrice.equals("0") || totalPrice.equals("0.00") || totalPrice.equals("RM 0") || totalPrice.equals("RM 0.00")) {
            btnPay.setClickable(false);  // Disable the button if the price is 0 or 0.00
        } else {
            btnPay.setClickable(true);   // Enable the button if the price is not 0
        }

        // Set click listener for the Pay button
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the total price from the TextView (or any other relevant data)
                String totalPrice = textSubTotal.getText().toString();  // Get the formatted total price
                String cartID = ""; // Initialize an empty cartID

                // Assuming that cartList has the cart item data, get the cartID from the first item in the list
                if (!cartList.isEmpty()) {
                    cartID = cartList.get(0).getCartID(); // Get the first cartID (if applicable)
                }

                // Pass total price, userID, and cartID to the PaymentMethod activity
                Intent intent = new Intent(CartProduct.this, PaymentMethod.class);
                String totalPriceWithoutRM = totalPrice.replace("RM", "").trim();
                intent.putExtra("TOTAL_PRICE", totalPriceWithoutRM);  // Pass the total price
                intent.putExtra("USER_ID", savedUserID);     // Pass the user ID
                intent.putExtra("CART_ID", cartID);         // Pass the cartID
                startActivity(intent);  // Start the PaymentMethod activity
            }
        });
    }

    private void fetchCartItems(String userID) {
        String url = getString(R.string.api_cart_item); // Update to your API URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                JSONArray items = jsonResponse.getJSONArray("cart_items");

                                cartList.clear();
                                double totalPrice = 0;
                                int totalQuantity = 0; // Initialize totalQuantity to 0

                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = items.getJSONObject(i);
                                    String cartItemID = item.getString("CartItemID");
                                    String cartID = item.getString("CartID");
                                    String drugID = item.getString("DrugID");
                                    String genericName = item.getString("GenericName");
                                    String drugImage = item.getString("DrugImage");
                                    int quantity = item.getInt("Quantity");
                                    double price = item.getDouble("Price");

                                    cartList.add(new Cart(cartItemID, cartID, drugID, genericName, drugImage, quantity, price));

                                    totalPrice += price * quantity;
                                    totalQuantity += quantity; // Add the quantity to totalQuantity
                                }

                                cartAdapter.notifyDataSetChanged();

                                // Update the total number of items (total quantity)
                                txtZero.setText(String.valueOf(totalQuantity));  // Display total quantity of all items

                                // Update the subtotal price
                                String totalPriceFormatted = String.format("%.2f", totalPrice);
                                textSubTotal.setText("RM" + totalPriceFormatted);
                                btnPay.setText("RM" + totalPriceFormatted);

                                updatePayButtonState("RM" + totalPriceFormatted);

                                // If cart is empty, show the empty cart layout, otherwise show the RecyclerView
                                if (cartList.isEmpty()) {
                                    recyclerView.setVisibility(View.GONE);
                                    emptyCartLayout.setVisibility(View.VISIBLE);
                                } else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    emptyCartLayout.setVisibility(View.GONE);
                                }
                            } else {
                                Toast.makeText(CartProduct.this, "Failed to fetch cart items.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("CartError", "JSON Parsing error", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CartError", "Volley error", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "fetch_cart_items");
                params.put("UserID", userID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }


    // Method to update item count and subtotal
    public void updateCartSummary() {
        double totalPrice = 0;
        int totalQuantity = 0;

        for (Cart cart : cartList) {
            totalPrice += cart.getPrice() * cart.getQuantity();
            totalQuantity += cart.getQuantity(); // Sum up the quantities of all items
        }

        txtZero.setText(String.valueOf(totalQuantity));  // Update the number of items
        String totalPriceFormatted = String.format("%.2f", totalPrice);
        textSubTotal.setText("RM" + totalPriceFormatted);  // Update the subtotal price
        btnPay.setText("RM" + totalPriceFormatted);  // Update the subtotal price

        updatePayButtonState(totalPriceFormatted); // Check if the total price is 0 or not
    }

    public void showEmptyCartView() {
        recyclerView.setVisibility(View.GONE);
        emptyCartLayout.setVisibility(View.VISIBLE);
    }

    // Add this helper method to handle the button's state based on price
    private void updatePayButtonState(String totalPrice) {
        // Remove the "RM" part before checking
        String totalPriceWithoutRM = totalPrice.replace("RM", "").trim();

        // Check if the totalPrice is "0" or "0.00"
        if (totalPriceWithoutRM.equals("0") || totalPriceWithoutRM.equals("0.00")) {
            btnPay.setClickable(false);  // Disable the button if the price is 0 or 0.00
        } else {
            btnPay.setClickable(true);   // Enable the button if the price is not 0
        }
    }



}

