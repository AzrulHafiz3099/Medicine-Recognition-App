package com.workshop2.medrecog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<Cart> cartList;

    public CartAdapter(Context context, List<Cart> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Cart cart = cartList.get(position);

        holder.textItemName.setText(cart.getGenericName());
        holder.textPriceValue.setText(String.valueOf(cart.getPrice()));
        holder.textQuantity.setText(String.valueOf(cart.getQuantity()));

        // Load the drug image using Glide
        Glide.with(context).load(cart.getDrugImage()).into(holder.imageParacetamol);

        // Set click listener for the trash icon
        holder.imageTrash.setOnClickListener(v -> deleteCartItem(cart.getCartItemID(), position));

        // Set click listener for the minus button
        holder.imageMinus.setOnClickListener(v -> {
            int newQuantity = cart.getQuantity() - 1;

            if (newQuantity >= 1) {
                updateQuantity(cart.getCartItemID(), newQuantity, position);
            } else {
                deleteCartItem(cart.getCartItemID(), position);
            }
        });

        // Set click listener for the plus button
        holder.imagePlus.setOnClickListener(v -> {
            int newQuantity = cart.getQuantity() + 1;
            updateQuantity(cart.getCartItemID(), newQuantity, position);
        });
    }

    private void updateCartSummary() {
        // Update the total number of items and subtotal price in CartProduct
        if (context instanceof CartProduct) {
            ((CartProduct) context).updateCartSummary();
        }
    }

    // Inside the deleteCartItem method of CartAdapter.java
    private void deleteCartItem(String cartItemID, int position) {
        String url = context.getString(R.string.api_cart_item); // API URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // Remove item from the cart list and notify the adapter
                                cartList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, cartList.size());
                                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                                updateCartSummary();  // Update subtotal and item count
                            } else {
                                Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
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
                params.put("action", "delete_cart_item");
                params.put("CartItemID", cartItemID); // Send CartItemID to delete
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }

    private void updateQuantity(String cartItemID, int newQuantity, int position) {
        String url = context.getString(R.string.api_cart_item); // API URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // Update the cart item in the list and notify the adapter
                                cartList.get(position).setQuantity(newQuantity);
                                notifyItemChanged(position); // Refresh the item in RecyclerView
                                Toast.makeText(context, "Quantity updated", Toast.LENGTH_SHORT).show();
                                updateCartSummary();  // Update subtotal and item count
                            } else {
                                Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show();
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
                params.put("action", "update_cart_item_quantity");
                params.put("CartItemID", cartItemID); // Send CartItemID to update
                params.put("Quantity", String.valueOf(newQuantity)); // New quantity
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }


    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textItemName, textPriceValue, textQuantity;
        ImageView imageParacetamol, imageTrash;
        LinearLayout imageMinus, imagePlus;

        public ViewHolder(View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.text_item_name);
            textPriceValue = itemView.findViewById(R.id.text_price_value);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            imageParacetamol = itemView.findViewById(R.id.image_paracetamol);
            imageTrash = itemView.findViewById(R.id.image_trash); // Add this line
            imageMinus = itemView.findViewById(R.id.container_minus);
            imagePlus = itemView.findViewById(R.id.container_plus);
        }
    }
}
