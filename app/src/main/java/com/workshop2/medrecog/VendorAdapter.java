package com.workshop2.medrecog;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.ViewHolder> {

    private Context context;
    private List<Vendor> vendorList;

    public VendorAdapter(Context context, List<Vendor> vendorList) {
        this.context = context;
        this.vendorList = vendorList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vendor_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Vendor vendor = vendorList.get(position);

        holder.nameTextView.setText(vendor.getName());
        holder.addressTextView.setText(vendor.getAddress());
        holder.contactTextView.setText(vendor.getContact());
        holder.emailTextView.setText(vendor.getEmail());

        // Set OnClickListener to navigate to next page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Homepage.class);
            intent.putExtra("VendorID", vendor.getId()); // Pass VendorID to next page
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, addressTextView, contactTextView, emailTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.vendor_fullname);
            addressTextView = itemView.findViewById(R.id.vendor_address);
            contactTextView = itemView.findViewById(R.id.vendor_contact);
            emailTextView = itemView.findViewById(R.id.vendor_email);
        }
    }
}
