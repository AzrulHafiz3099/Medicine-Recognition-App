package com.workshop2.medrecog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.workshop2.medrecog.R;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LocationItem> locationList;
    private OnItemClickListener listener;

    public LocationAdapter(List<LocationItem> locationList, OnItemClickListener listener) {
        this.locationList = locationList;
        this.listener = listener;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_card, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        LocationItem item = locationList.get(position);
        holder.storeName.setText(item.getName());
        holder.storeDistance.setText(item.getDistance());
        holder.storeStatus.setText(item.getStatus());
        holder.storeOpeningTime.setText(item.getOpeningTime());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        holder.getDirectionsButton.setOnClickListener(v -> listener.onGetDirectionsClick(position));
        holder.selectStoreButton.setOnClickListener(v -> listener.onSelectStoreClick(position));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onGetDirectionsClick(int position);
        void onSelectStoreClick(int position);
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView storeName, storeDistance, storeStatus, storeOpeningTime;
        TextView getDirectionsButton, selectStoreButton;

        public LocationViewHolder(View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.store_name);
            storeDistance = itemView.findViewById(R.id.store_distance);
            storeStatus = itemView.findViewById(R.id.store_status);
            storeOpeningTime = itemView.findViewById(R.id.store_opening_time);
            getDirectionsButton = itemView.findViewById(R.id.get_directions_button);
            selectStoreButton = itemView.findViewById(R.id.select_store_button);
        }
    }
}
