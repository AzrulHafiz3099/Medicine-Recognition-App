package com.workshop2.medrecog;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.workshop2.medrecog.adapter.TrackingAdapter;
import com.workshop2.medrecog.adapter.TrackingItem;
import com.workshop2.medrecog.adapter.TrackingStep;

import java.util.ArrayList;
import java.util.List;

public class OrderList extends AppCompatActivity {

    private RecyclerView trackingRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list);

        // Sample data
        List<TrackingItem> trackingItems = new ArrayList<>();
        trackingItems.add(createTrackingItem("Order 1234", "2023-01-03", 2, "$50.00", createTrackingSteps()));
        trackingItems.add(createTrackingItem("Order 5678", "2023-01-05", 1, "$25.99", createTrackingSteps()));


        trackingRecyclerView = findViewById(R.id.tracking_recycler_view);
        trackingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trackingRecyclerView.setAdapter(new TrackingAdapter(trackingItems));
    }

    // Helper methods (same as before)
    private List<TrackingStep> createTrackingSteps() {
        List<TrackingStep> steps = new ArrayList<>();
        steps.add(new TrackingStep("Placed", "2023-01-03", true));
        steps.add(new TrackingStep("Picked Up", "pending", false)); // Date is "pending"
        steps.add(new TrackingStep("Out for Delivery", null, false)); // Date is null
        return steps;
    }

    private TrackingItem createTrackingItem(String orderNumber, String placedDate, int itemCount, String itemTotal, List<TrackingStep> steps) {
        return new TrackingItem(orderNumber, placedDate, itemCount, itemTotal, steps);
    }

    //Helper method to find the tracking item by order id
    private TrackingItem findTrackingItemByOrderId(List<TrackingItem> trackingItems, String orderId) {
        for (TrackingItem item : trackingItems) {
            if (item.orderNumber.equals(orderId)) {
                return item;
            }
        }
        return null; // Return null if not found
    }

    //Helper method to get tracking items
    private List<TrackingItem> getTrackingItems(){
        List<TrackingItem> trackingItems = new ArrayList<>();
        trackingItems.add(createTrackingItem("Order 1234", "2023-01-03", 2, "$50.00", createTrackingSteps()));
        trackingItems.add(createTrackingItem("Order 5678", "2023-01-05", 1, "$25.99", createTrackingSteps()));
        return trackingItems;
    }
}