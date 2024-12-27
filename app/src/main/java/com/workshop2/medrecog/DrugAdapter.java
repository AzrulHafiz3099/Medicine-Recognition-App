package com.workshop2.medrecog;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.ViewHolder> {

    private Context context;
    private List<Drug> drugList;
    private String userID; // Add a userID field

    // Update the constructor to include userID
    public DrugAdapter(Context context, List<Drug> drugList, String userID) {
        this.context = context;
        this.drugList = drugList;
        this.userID = userID; // Initialize userID
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.drug_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drug drug = drugList.get(position);
        holder.name.setText(drug.getName());
        holder.price.setText(String.format("RM %.2f", drug.getPrice()));

        // Load image using Glide
        Glide.with(context)
                .load(drug.getImage())
                .placeholder(R.drawable.default_profile_picture)
                .into(holder.image);

        // Set click listener to start new activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDesc.class);
            intent.putExtra("SupplyID", drug.getId()); // Pass SupplyID to the next activity
            intent.putExtra("userID", userID); // Pass userID to the next activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.drug_name);
            price = itemView.findViewById(R.id.drug_price);
            image = itemView.findViewById(R.id.drug_image);
        }
    }
}

