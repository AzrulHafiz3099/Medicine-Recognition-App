package com.workshop2.medrecog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Addremindertimepick extends AppCompatActivity {

    private TextView selectedTimeTextView;
    private NumberPicker hourPicker, minutePicker, ampmPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addremindertimepick);

        // Initialize Views
//        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        ampmPicker = findViewById(R.id.ampmPicker);

        // Configure Hour Picker
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);

        // Configure Minute Picker
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(value -> String.format("%02d", value));

        // Configure AM/PM Picker
        String[] ampmValues = {"AM", "PM"};
        ampmPicker.setMinValue(0);
        ampmPicker.setMaxValue(1);
        ampmPicker.setDisplayedValues(ampmValues);

        // OK Button to Confirm Selection
//        okButton.setOnClickListener(v -> {
//            int hour = hourPicker.getValue();
//            int minute = minutePicker.getValue();
//            String ampm = ampmValues[ampmPicker.getValue()];
//            String selectedTime = String.format("%02d:%02d %s", hour, minute, ampm);
//
//            // Display Selected Time
//            selectedTimeTextView.setText("Selected Time: " + selectedTime);
//        });
    }
}
