package com.workshop2.medrecog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.workshop2.medrecog.R;

import java.util.List;


public class TrackingAdapter extends RecyclerView.Adapter<TrackingAdapter.ViewHolder> {

    private List<TrackingItem> trackingItems;

    public TrackingAdapter(List<TrackingItem> trackingItems) {
        this.trackingItems = trackingItems;
    }

    @NonNull // This annotation was missing!
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracking_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackingItem item = trackingItems.get(position);

        holder.orderNumber.setText(item.orderNumber);
        holder.orderPlacedDate.setText(item.placedDate);
        holder.orderItems.setText("Items: " + item.itemCount + " | Total: " + item.itemTotal);

        // Clear previous status items
        holder.statusContainer.removeAllViews();


        // Dynamically add status items to the LinearLayout
        for (TrackingStep step : item.trackingSteps) {
            View statusView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.tracking_step_item, null);

            TextView statusText = statusView.findViewById(R.id.status_text);
            TextView statusDate = statusView.findViewById(R.id.status_date);
            View statusCircle = statusView.findViewById(R.id.status_circle);

            statusText.setText(step.status);

            // Check if the date is available (not "pending" or empty)
            if (step.date != null && !step.date.equalsIgnoreCase("pending") && !step.date.isEmpty()) {
                statusDate.setText(step.date);
                statusCircle.setBackgroundResource(R.drawable.ic_circle_filled); // Filled circle
            } else {
                statusDate.setText("Pending"); // Or leave it empty "" if you prefer
                statusCircle.setBackgroundResource(R.drawable.ic_circle_empty); // Gray circle
            }

            holder.statusContainer.addView(statusView);
        }

        holder.dropdownArrow.setOnClickListener(v -> {
            if (holder.statusContainer.getVisibility() == View.GONE) {
                holder.statusContainer.setVisibility(View.VISIBLE);
                holder.dropdownArrow.setImageResource(R.drawable.ic_expand_less); // Change icon
            } else {
                holder.statusContainer.setVisibility(View.GONE);
                holder.dropdownArrow.setImageResource(R.drawable.ic_expand_more); // Change icon
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackingItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderNumber;
        TextView orderPlacedDate;
        TextView orderItems;
        LinearLayout statusContainer;
        ImageView dropdownArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderNumber = itemView.findViewById(R.id.order_number);
            orderPlacedDate = itemView.findViewById(R.id.order_placed_date);
            orderItems = itemView.findViewById(R.id.order_items);
            statusContainer = itemView.findViewById(R.id.status_container);
            dropdownArrow = itemView.findViewById(R.id.dropdown_arrow);
        }
    }
}