package com.workshop2.medrecog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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

        holder.nameTextView.setText(vendor.getFullname());
        holder.addressTextView.setText(vendor.getAddress());
        holder.contactTextView.setText(vendor.getContactNumber());
        holder.emailTextView.setText(vendor.getEmail());

        // Get the profile picture URL
        String imageUrl = context.getString(R.string.vendor_image_url) + vendor.getProfilePicture();

        // Use Glide to load the image into the ImageView
        Glide.with(context)
                .load(imageUrl)  // The image URL
                .placeholder(R.drawable.placeholder_image)  // Optional placeholder
                .error(R.drawable.error_image)  // Optional error image
                .into(holder.vendorImage);

        // Set OnClickListener to navigate to the next page
        holder.itemView.setOnClickListener(v -> {
            // Call onVendorItemClick from the activity to handle the navigation
            ((StoreSelect) context).onVendorItemClick(vendor);
        });
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, addressTextView, contactTextView, emailTextView;
        ImageView vendorImage;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.vendor_fullname);
            addressTextView = itemView.findViewById(R.id.vendor_address);
            contactTextView = itemView.findViewById(R.id.vendor_contact);
            emailTextView = itemView.findViewById(R.id.vendor_email);
            vendorImage = itemView.findViewById(R.id.vendor_image);
        }
    }
}
